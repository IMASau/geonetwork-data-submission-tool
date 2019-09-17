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

from backend.models import Person


logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh person list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                Person.objects.all().filter(isUserAdded=False).delete()

                tern_persons = self._fetch_tern_data('person')
                if not tern_persons:
                    raise CommandError('No TERN persons found, assuming error; aborting')

                new_persons = []

                for person in tern_persons:
                    #if a user added person with the same uri exists delete them
                    try:
                        inst = Person.objects.get(uri=person.uri)
                        inst.delete()
                    except Person.DoesNotExist:
                        pass
                    new_persons.append(person)

                Person.objects.bulk_create(new_persons)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(Person).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed persons list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} persons".format(Person.objects.count()))
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
        _vocabServer = 'http://linkeddata.tern.org.au/viewer/tern/id/http:/linkeddata.tern.org.au/def/'
        _query = '_view=skos&_format=application/rdf+xml'
        url = '{base}{vocabName}?{query}'.format(base=_vocabServer,vocabName=VocabName,query=_query)
        graph = Graph()
        graph.parse(url, format='application/rdf+xml')
        _type_pred = URIRef('http://www.w3.org/1999/02/22-rdf-syntax-ns#type')
        _person_object = URIRef('http://schema.org/Person')
        _familyName_pred = URIRef('http://schema.org/familyName')
        _givenName_pred = URIRef('http://schema.org/givenName')
        _honorificPrefix_pred = URIRef('http://schema.org/honorificPrefix')
        _orgUri_pred = URIRef('http://schema.org/memberOf')
        _orcid_pred = URIRef('http://schema.org/sameAs')
        _email_pred = URIRef('http://schema.org/email')
        _prefLabel_pred = URIRef('http://www.w3.org/2004/02/skos/core#prefLabel')
        persons = graph.subjects(_type_pred, _person_object)
        for subject in persons:
            uri = subject.toPython()
            familyName = Command.pred_value_or_empty(graph, subject, _familyName_pred)
            givenName = Command.pred_value_or_empty(graph, subject, _givenName_pred)
            honorificPrefix = Command.pred_value_or_empty(graph, subject, _honorificPrefix_pred)
            orgUri = Command.pred_value_or_empty(graph, subject, _orgUri_pred)
            prefLabel = Command.pred_value_or_empty(graph, subject, _prefLabel_pred)
            email = Command.pred_value_or_empty(graph, subject, _email_pred)
            orcid = Command.pred_value_or_empty(graph, subject, _orcid_pred)
            orcid = orcid.replace('https://orcid.org/','')
            orcid = orcid.replace('http://orcid.org/','')
            # check that it's an orcid
            if re.match(r"0000-000(1-[5-9]|2-[0-9]|3-[0-4])\d\d\d-\d\d\d[\dX]", orcid) is None:
                orcid = ''
            yield Person(
                uri=uri,
                orgUri=orgUri,
                familyName=familyName,
                givenName=givenName,
                honorificPrefix=honorificPrefix,
                orcid=orcid,
                electronicMailAddress=email,
                prefLabel=prefLabel,
                isUserAdded=False
            )
