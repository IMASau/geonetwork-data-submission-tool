import csv
import datetime
import io
import logging
import urllib

from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
import requests

from metcalf.imas.backend.models import GeographicExtentKeyword


logger = logging.getLogger(__name__)


class Command(BaseCommand):
    help = 'Refresh geographic-extents list from online vocab'

    VocabName = 'aodn-geographic-extents-vocabulary'
    TopCategory = 'http://vocab.aodn.org.au/def/geographicextents/1'
    VocabVersion = 'version-4-0'
    VocabServer = 'http://vocabs.ardc.edu.au/repository/api/sparql/aodn_'

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                GeographicExtentKeyword.objects.all().delete()

                keywords = self.process_keywords()

                GeographicExtentKeyword.objects.bulk_create(keywords)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(GeographicExtentKeyword).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed geographic-extent keyword list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} geographic-extent keywords".format(GeographicExtentKeyword.objects.count()))
        except:
            import traceback
            logger.error(traceback.format_exc())
            raise

    def process_keywords(self):
        base_keywords = self._fetch_vocab_data(self.VocabServer, self.VocabName, self.VocabVersion, self.TopCategory)
        if not base_keywords:
            raise CommandError('No keywords found, assuming error; aborting')
        allRows = {}
        for uri, data in base_keywords:
            allRows[uri] = data

        chains = []

        for key in allRows.keys():
            chain = [self._make_pk(allRows[key]['URI'])]
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
            keyword = GeographicExtentKeyword()
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

    def _fetch_vocab_data(self, vocabServer, vocabName, version, topCategory):
        """Returns a generator of triples of the URI, the parent URI
        (nullable), and the data as a dictionary, created from the
        current AODN vocab."""

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
        url = '{base}{vocabName}_{version}?query={query}'.format(base=vocabServer,
                                                                 vocabName=vocabName,
                                                                 version=version,
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

    def _make_pk(self, uri):
        return uri.split('/')[-1]

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
