import csv
import datetime
import logging
import urllib
import io

import requests
from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import CommandError
from django.db import transaction

import re


logger = logging.getLogger(__name__)


# FIXME this mentions tern
class AodnBaseParameterVocabLoader(object):
    """Mixin class to implement the generic parameter-* loaders"""

    # Classes using this mixin should provide attributes for:
    # * CategoryVocab (name of the main vocab, excluding server and
    #   version and aodn_ prefix, but may be null.  Concepts in this
    #   vocab are not selectable)
    # * ParameterVocab (the base vocab name, excluding server and
    #   version and aodn_ prefix.  Concepts in this vocab are
    #   selectable)
    # * ParameterClass (eg, ParameterName)
    # * HumanName (eg "parameter-name", used in logging etc)

    # * help (ie, the usual manage-command attribute)

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    # We have at least 2 vocabs involved for some concepts (currently
    # 2, but possibly there can be more), so we'll treat that as a
    # list of vocabs to load.  Build up the hierarchy in-memory, then
    # we can use the load_bulk() method.

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        entries = {}            # keyed by URI, values in the format expected by load_bulk
        hierarchy = []          # The root nodes, will be passed to load_bulk

        vocab_list = [(self.CategoryVocab, False),
                      (self.ParameterVocab, True)]

        entry_count = 0
        for vocab,selectable in vocab_list:
            if not vocab:
                continue
            version = self._fetch_version(vocab)
            for uri,parenturi,data in self._fetch_data(vocab, selectable, version):
                entry_count = entry_count + 1
                entry = self._merge_or_create(entries, uri, data)
                if not parenturi:
                    # If we don't have a parent URI, add to the list of roots:
                    hierarchy.append(entry)
                else:
                    # Otherwise, add in as a child in the appropriate place:
                    parentEntry = self._merge_or_create(entries, parenturi, None)
                    parentEntry['children'].append(entry)

        try:
            if entry_count == 0:
                raise CommandError('No TERN persons found, assuming error; aborting')
            with transaction.atomic():

                transaction.get_connection().cursor().execute(
                    "DELETE FROM {}".format(self.ParameterClass._meta.db_table))

                self.ParameterClass.load_bulk(hierarchy)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(self.ParameterClass).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed {} list - {:%Y-%m-%d %H:%M}'.format(self.HumanName,
                                                                                datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} {}".format(self.ParameterClass.objects.count(),
                                                        self.HumanName))
        except:
            import traceback
            logger.error('Importing for {}; {}', self.ParameterClass.__name__, traceback.format_exc())
            raise

    @staticmethod
    def _admin_pk(**options):
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
    def _fetch_data(VocabName, selectable, version):
        """Returns a generator of triples of the URI, the parent URI
        (nullable), and the data as a dictionary, created from the
        current AODN vocab."""
        _vocabServer = 'http://vocabs.ands.org.au/repository/api/sparql/aodn_'
        # Key concepts in this query: definition isn't present in
        # every entry so must be OPTIONAL, and the parent concept is
        # both OPTIONAL and can be specified in two different ways
        # (depending on whether the parent entry appears in the same
        # vocab or not):
        _query = urllib.parse.quote('PREFIX skos: <http://www.w3.org/2004/02/skos/core#>'
                                    ' SELECT ?uri ?name ?definition ?parent ?extParent WHERE {'
                                    '?uri skos:prefLabel ?name . '
                                    'OPTIONAL { ?uri skos:definition ?definition } . '
                                    'OPTIONAL { ?uri skos:broader ?parent } . '
                                    'OPTIONAL { ?uri skos:broadMatch ?extParent }'
                                    '}')
        url = '{base}{vocabName}_current?query={query}'.format(base=_vocabServer,
                                                               vocabName=VocabName,
                                                               query=_query)

        response = requests.get(url, headers={'Accept': 'text/csv'})
        reader = csv.DictReader(io.StringIO(response.text, newline=""), skipinitialspace=True)
        for row in reader:
            parent = row['parent'] or row['extParent']
            yield row['uri'], parent, {
                'URI': row['uri'],
                'Name': row['name'],
                'Definition': row['definition'],
                'is_selectable': selectable,
                'Version': version
            }

    @staticmethod
    def _fetch_version(VocabName):
        """Parse the vocab version from a linked-data endpoint. Slightly hacky,
        but works."""
        url = 'http://vocabs.ands.org.au/repository/api/lda/aodn/{vocab}/current/concept.json'.format(vocab=VocabName)
        response = requests.get(url)
        rjson = response.json()
        aboutstr = rjson['result']['_about']
        vmatch = re.search(r'version-[-0-9]+', aboutstr)
        return vmatch.group(0)

    @staticmethod
    def _merge_or_create(entries, uri, data):
        """Look up an entry and return it, ensuring it now exists in at least
        dummy form."""
        entry = entries.get(uri, {'data':None, 'children': []})
        entry['data'] = data or entry['data']
        entries[uri] = entry
        return entry
