import csv
import io

import datetime
import logging
import urllib
import urllib.parse

import requests
from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from rdflib import Graph, URIRef
from urllib.request import urlopen

from lxml import etree

from backend.models import SamplingFrequency


logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh sampling frequencies list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                SamplingFrequency.objects.all().delete()

                src_url = 'https://gcmdservices.gsfc.nasa.gov/kms/concepts/concept_scheme/temporalresolutionrange?format=xml'
                sampling_frequencies = []
                with urlopen(src_url) as f:
                    tree = etree.parse(f)
                    freqs = tree.findall('conceptBrief')
                    for freq in freqs:
                        isLeaf = freq.attrib['isLeaf']
                        if isLeaf == 'true':
                            uuid = freq.attrib['uuid']
                            uri = 'https://gcmd.nasa.gov/kms/concept/{uuid}.xml'.format(uuid=uuid)
                            prefLabel = freq.attrib['prefLabel']
                            prefLabelSortText = freq.attrib.get('prefLabelSortText', prefLabel)
                            sampling_frequencies.append(SamplingFrequency(uri=uri, prefLabel=prefLabel, prefLabelSortText=prefLabelSortText))


                SamplingFrequency.objects.bulk_create(sampling_frequencies)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(SamplingFrequency).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed sampling frequency list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} sampling frequencies".format(SamplingFrequency.objects.count()))
        except:
            import traceback
            logger.error(traceback.format_exc())
            raise


    def _admin_pk(self, **options):
        "Normalise the admin user id, guessing if not specified"
        adminpk = options.get('admin_id', None)
        if adminpk:
            try:
                User.objects.filter(pk=adminpk, is_staff=True)
            except ObjectDoesNotExist:
                raise CommandError('Invalid admin user id specified: {id}'.format(id=adminpk))
        else:
            # Look for the first staff member:
            adminuser = User.objects.filter(is_staff=True).first()
            if not adminuser:
                raise CommandError('No admin user found; create one first')
            adminpk = adminuser.pk
        return adminpk

    @staticmethod
    def pred_value_or_empty(graph, subject, predicate):
        retval = next(graph.objects(subject, predicate), None)
        if retval is not None:
            return retval.toPython()
        else:
            return ''

    @staticmethod
    def _fetch_tern_data(VocabName):
        #TODO: Make this generic for all the ands vocabs that aren't special
        _vocabServer = 'http://vocabs.ands.org.au/repository/api/sparql/'
        _query = urllib.parse.quote('CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }')
        url = '{base}{vocabName}_current?query={query}'.format(base=_vocabServer,vocabName=VocabName,query=_query)
        graph = Graph()
        graph.parse(url, format='application/rdf+xml')
        _type_pred = URIRef('http://www.w3.org/1999/02/22-rdf-syntax-ns#type')
        _resource_obj = URIRef("http://www.w3.org/2004/02/skos/core#Concept")
        _prefLabel_pred = URIRef('http://www.w3.org/2004/02/skos/core#prefLabel')
        freqs = graph.subjects(_type_pred, _resource_obj)
        for subject in freqs:
            uri = subject.toPython()
            prefLabel = Command.pred_value_or_empty(graph, subject, _prefLabel_pred)
            yield SamplingFrequency(
                uri=uri,
                prefLabel=prefLabel
            )