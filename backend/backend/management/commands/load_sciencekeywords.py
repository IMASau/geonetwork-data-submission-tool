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
from lxml import etree
from urllib.request import urlopen

from backend.models import ScienceKeyword, AnzsrcKeyword

logger = logging.getLogger(__name__)


class Command(BaseCommand):
    help = 'Refresh topic categories list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def make_anzsrc_pk(self, uri):
        return uri

    def make_sciencekeyword_pk(self, uri):
        return uri.split('/')[-1]

    def process_keywords(self, vocab_name, pk_fn, version, objName, topCategory):
        base_keywords = self._fetch_tern_data(vocab_name, version, topCategory)
        if not base_keywords:
            raise CommandError('No keywords found, assuming error; aborting')
        allRows = {}
        for uri, data in base_keywords:
            allRows[uri] = data

        chains = []

        for key in allRows.keys():
            chain = [pk_fn(allRows[key]['URI'])]
            chain.insert(1, allRows[key]['Name'])
            parent = allRows[key]['Parent']
            while parent:
                chain.insert(1, allRows[parent]['Name'])
                parent = allRows[parent]['Parent']
            while len(chain) < 8:
                chain.append('')
            chain.append(allRows[key]['URI'])
            chains.append(chain)

        keywords = []
        for chain in chains:
            keyword = eval(objName)()
            keyword.UUID = chain[0]
            keyword.Category = chain[1]
            keyword.Topic = chain[2]
            keyword.Term = chain[3]
            keyword.VariableLevel1 = chain[4]
            keyword.VariableLevel2 = chain[5]
            keyword.VariableLevel3 = chain[6]
            keyword.DetailedVariable = chain[7]
            keyword.uri = chain[8]
            keywords.append(keyword)

        if not keywords:
            raise CommandError('No keywords found, assuming error; aborting')

        return keywords

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                ScienceKeyword.objects.all().delete()

                keywords = self.process_keywords('ardc-curated_gcmd-sciencekeywords_8-6-2018-12-17',
                                                 self.make_sciencekeyword_pk,
                                                 '8-6-2018-12-17',
                                                 'ScienceKeyword',
                                                 'https://gcmdservices.gsfc.nasa.gov/kms/concept/1eb0ea0a-312c-4d74-8d42-6f1ad758f999')

                ScienceKeyword.objects.bulk_create(keywords)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(ScienceKeyword).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed GCMD keyword list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} GCMD keywords".format(ScienceKeyword.objects.count()))
        except:
            import traceback
            logger.error(traceback.format_exc())
            raise

        try:
            with transaction.atomic():
                AnzsrcKeyword.objects.all().delete()

                keywords = self.process_keywords('anzsrc-for', self.make_anzsrc_pk,
                                                 '2008', 'AnzsrcKeyword',
                                                 'http://purl.org/au-research/vocabulary/anzsrc-for/2008/')

                AnzsrcKeyword.objects.bulk_create(keywords)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(ScienceKeyword).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed ANZSRC keyword list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} ANZSRC keywords".format(AnzsrcKeyword.objects.count()))
        except:
            import traceback
            logger.error(traceback.format_exc())
            raise

    def _fetch_tern_data(self, VocabName, version, topCategory):
        """Returns a generator of triples of the URI, the parent URI
        (nullable), and the data as a dictionary, created from the
        current AODN vocab."""
        _vocabServer = 'http://vocabs.ands.org.au/repository/api/sparql/'
        # Key concepts in this query: definition isn't present in
        # every entry so must be OPTIONAL, and the parent concept is
        # both OPTIONAL and can be specified in two different ways
        # (depending on whether the parent entry appears in the same
        # vocab or not):
        _query = urllib.parse.quote('PREFIX skos: <http://www.w3.org/2004/02/skos/core#>'
                                    ' SELECT ?uri ?name ?definition ?parent ?extParent ?top WHERE {'
                                    '?uri skos:prefLabel ?name . '
                                    'OPTIONAL { ?uri skos:definition ?definition } . '
                                    'OPTIONAL { ?uri skos:broader ?parent } . '
                                    'OPTIONAL { ?uri skos:broadMatch ?extParent } . '
                                    'OPTIONAL { ?uri skos:topConceptOf ?top}'
                                    '}')
        url = '{base}{vocabName}?query={query}'.format(base=_vocabServer,
                                                       vocabName=VocabName,
                                                       query=_query)

        response = requests.get(url, headers={'Accept': 'text/csv'})
        reader = csv.DictReader(io.StringIO(response.text, newline=""), skipinitialspace=True)
        for row in reader:
            parent = row['parent'] or row['extParent']
            if row['uri'] == topCategory:
                continue
            if parent == topCategory:
                parent = ''
            yield row['uri'], {
                'URI': row['uri'],
                'Name': row['name'],
                'Parent': parent,
                'Definition': row['definition'],
                'is_selectable': True,
                'Version': version
            }

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
