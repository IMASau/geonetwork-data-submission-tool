import csv
import io

import datetime
import logging
import urllib
import urllib.parse
import re

import requests
from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from rdflib import Graph, URIRef

from backend.models import Institution


logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh institutions list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                Institution.objects.all().filter(isUserAdded=False).delete()

                tern_institutions = self._fetch_sparql()
                if not tern_institutions:
                    raise CommandError('No TERN organisations found, assuming error; aborting')

                new_institutions = []
                institution_count = 0
                for tern_institution in tern_institutions:
                    institution_count = institution_count + 1
                    tern_institution.organisationName = tern_institution.altLabel or tern_institution.prefLabel
                    #if a user added institutions with the same uri exists delete them
                    try:
                        inst = Institution.objects.get(uri=tern_institution.uri)
                        inst.delete()
                    except Institution.DoesNotExist:
                        pass
                    new_institutions.append(tern_institution)

                if institution_count == 0:
                    raise CommandError('No TERN institutions found, assuming error; aborting')

                Institution.objects.bulk_create(new_institutions)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(Institution).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed institutions list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} institutions".format(Institution.objects.count()))

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
    def _fetch_sparql():
        _query = urllib.parse.quote('PREFIX schema: <http://schema.org/> '
                                    'PREFIX tern-org: <https://w3id.org/tern/ontologies/org/> '
                                    ' PREFIX : <https://w3id.org/tern/resources/> '
                                    ' PREFIX prov: <http://www.w3.org/ns/prov#> '
                                    ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
                                    ' PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> '
                                    ' select * '
                                    ' from <https://w3id.org/tern/resources/> '
                                    ' where { '
                                    '     ?org a schema:Organization . '
                                    '     ?org rdfs:label ?name . '
                                    '     OPTIONAL { '
                                    '         ?org schema:address ?postalAddress . '
                                    '         ?postalAddress tern-org:fullAddressLine ?fullAddressLine ; '
                                    '                        schema:streetAddress ?streetAddress ; '
                                    '                        schema:addressLocality ?addressLocality ; '
                                    '                        schema:addressRegion ?addressRegion ; '
                                    '                        schema:postalCode ?postalCode ; '
                                    '                        schema:addressCountry ?addressCountry . '
                                    '         OPTIONAL { '
                                    '             ?postalAddress schema:postOfficeBoxNumber ?POBox . '
                                    '         } '
                                    '     } '
                                    ' } '
                                    ' ORDER BY ASC(?name)')
        url = "https://graphdb-850.tern.org.au/repositories/knowledge_graph_core?query={query}".format(query=_query)
        response = requests.get(url, headers={'Accept': 'text/csv'})
        if not response.ok:
            raise CommandError('Error loading the institutions vocabulary. Aborting. Error was {}'.format(response.content))
        reader = csv.DictReader(io.StringIO(response.text, newline=""), skipinitialspace=True)
        for row in reader:
            yield Institution (
                uri=row['org'],
                prefLabel=row['name'],
                altLabel=row['name'],
                deliveryPoint=row['streetAddress'],
                postalCode=row['postalCode'],
                country=row['addressCountry'],
                city=row['addressLocality'],
                administrativeArea=row.get('addressRegion',''),
                #not present in TERN data
                exactMatch='',
                isUserAdded=False
            )
        return

#http://linkeddata.tern.org.au/viewer/tern/id/http:/linkeddata.tern.org.au/def/Person?_view=skos&_format=application/rdf+xml
    @staticmethod
    def _fetch_tern_data(VocabName):
        _pred_prefix = 'http://www.w3.org/2004/02/skos/core#'
        _vocabServer = 'http://linkeddata.tern.org.au/viewer/tern/id/http:/linkeddata.tern.org.au/def/'
        _query = '_view=skos&_format=application/rdf+xml'
        url = '{base}{vocabName}?{query}'.format(base=_vocabServer,vocabName=VocabName,query=_query)
        graph = Graph()
        graph.parse(url, format='application/rdf+xml')
        _type_pred = URIRef('http://www.w3.org/1999/02/22-rdf-syntax-ns#type')
        _org_object = URIRef('http://schema.org/Organization')
        _parent_pred = URIRef(_pred_prefix + 'broader')
        _preflabel_pred = URIRef(_pred_prefix + 'prefLabel')
        _altlabel_pred = URIRef(_pred_prefix + 'altLabel')
        orgs = graph.subjects(_type_pred, _org_object)
        for subject in orgs:
            uri = subject.toPython()
            parent = next(graph.objects(subject, _parent_pred), None)
            if parent is not None:
                parent = parent.toPython()
            altLabel = next(graph.objects(subject, _altlabel_pred), None)
            if altLabel is not None:
                altLabel = altLabel.toPython()
            else:
                altLabel = ''
            preflabel = next(graph.objects(subject, _preflabel_pred), None)
            if preflabel is not None:
                preflabel = preflabel.toPython()
            else:
                preflabel = ''
            yield Institution(
                uri=uri,
                prefLabel=preflabel,
                altLabel=altLabel,
                #not present in TERN data
                exactMatch='',
                isUserAdded=False
            )