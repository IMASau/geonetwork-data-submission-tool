import csv
import datetime
import io
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
from django.utils.encoding import smart_text

from metcalf.imas.backend.models import Institution

logger = logging.getLogger(__name__)


def load_edmo_address(aodn_institution, edmo_address):
    address2_present = len(edmo_address['Address2']) > 0
    country_is_australia = edmo_address['Country'] == 'Australia'

    # Case 1
    if not address2_present:
        aodn_institution.deliveryPoint = edmo_address['Address']
        aodn_institution.postalCode = edmo_address['Zipcode']
        aodn_institution.city = edmo_address['City']
        aodn_institution.administrativeArea = edmo_address['State']
        aodn_institution.country = edmo_address['Country']

    # Case 2
    elif address2_present and not country_is_australia:
        aodn_institution.deliveryPoint = edmo_address['Address']
        aodn_institution.deliveryPoint2 = edmo_address['Address2']
        aodn_institution.postalCode = edmo_address['Zipcode']
        aodn_institution.city = edmo_address['City']
        aodn_institution.administrativeArea = edmo_address['State']
        aodn_institution.country = edmo_address['Country']

    # Case 3
    else:

        raw_address = edmo_address['Address2']

        if ',' not in raw_address:
            raise CommandError(
                "Case 3: Can't split address on ','.  Value is: '{}'".format(raw_address))

        (address_city, administrativeArea_postalCode) = raw_address.rsplit(",", 1)
        address_city = address_city.strip()
        administrativeArea_postalCode = administrativeArea_postalCode.strip()

        if ',' not in address_city:
            raise CommandError(
                "Case 3: Can't split address_city chunk on ','. Value is '{}'".format(address_city))

        (address, city) = address_city.rsplit(',', 1)
        address = address.strip()
        city = city.strip()

        if ' ' not in administrativeArea_postalCode:
            raise CommandError(
                "Case 3: Can't split administrativeArea_postalCode chunk on ' '. Value is '{}'"
                    .format(administrativeArea_postalCode)
            )

        (administrativeArea, postalCode) = administrativeArea_postalCode.rsplit(' ', 1)
        administrativeArea = administrativeArea.strip()
        postalCode = postalCode.strip()

        if len(postalCode) != 4:
            raise CommandError(
                "Case 3: postalCode isn't 4 characters long. Value is '{}'"
                    .format(postalCode)
            )

        aodn_institution.deliveryPoint = address
        aodn_institution.postalCode = postalCode
        aodn_institution.city = city
        aodn_institution.administrativeArea = administrativeArea
        aodn_institution.country = edmo_address['Country']


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
                Institution.objects.all().delete()

                aodn_institutions = self._fetch_aodn_organisations()
                if not aodn_institutions:
                    raise CommandError('No AODN organisations found, assuming error; aborting')

                aodn_institutions = [i for i in aodn_institutions]

                edmo_addresses = self._fetch_edmo_addresses()
                if not edmo_addresses:
                    raise CommandError('No EDMX institutions found, assuming error; aborting')

                edmo_address_lookup = {addr['URL']: addr for addr in edmo_addresses}
                for aodn_institution in aodn_institutions:

                    aodn_institution.organisationName = \
                        aodn_institution.altLabel or aodn_institution.prefLabel

                    if not aodn_institution.exactMatch:
                        continue

                    edmo_address = edmo_address_lookup.get(aodn_institution.exactMatch, None)

                    if edmo_address:
                        try:
                            load_edmo_address(aodn_institution, edmo_address)
                        except CommandError as e:
                            self.stdout.write('\nData validation check failed processing edmo_address for {}'.format(
                                aodn_institution))
                            self.stdout.write('\n{}'.format(str(e)))
                            self.stdout.write('\n{}'.format(edmo_address))
                            self.stdout.write('\n')

                        # except Exception, e:
                        #     self.stdout.write('\nError processing edmo_address for {}'.format(aodn_institution))
                        #     self.stdout.write('\n{}'.format(str(e)))
                        #     self.stdout.write('\n{}'.format(edmo_address))
                        #     self.stdout.write('\n')
                        #     raise e

                    else:
                        self.stdout.write('\nNo AODN address found for {}'.format(aodn_institution))
                        self.stdout.write('\n')

                Institution.objects.bulk_create(aodn_institutions)

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
        """Normalise the admin user id, guessing if not specified"""
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

    def _fetch_aodn_organisations(self):
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
                                    ' SELECT ?uri ?prefLabel ?altLabel ?exactMatch WHERE {'
                                    '?uri skos:prefLabel ?prefLabel . '
                                    'OPTIONAL { ?uri skos:altLabel ?altLabel } . '
                                    'OPTIONAL { ?uri skos:exactMatch ?exactMatch }'
                                    '}')
        url = '{base}{vocabName}_current?query={query}'.format(base=_vocabServer,
                                                               vocabName='aodn-organisation-vocabulary',
                                                               query=_query)

        # We used to stream the response, but they've turned on gzip
        # it seems, and apparently `requests' doesn't automatically
        # decode gzip in that case.  To keep it simple (and in case
        # there's further changes), we'll just work with the text
        # (it's not that large)
        response = requests.get(url, headers={'Accept': 'text/csv'})
        # reader = csv.DictReader(response.text.encode('utf8').splitlines(), skipinitialspace=True)
        reader = csv.DictReader(io.StringIO(response.text, newline=""), skipinitialspace=True)
        for row in reader:
            yield Institution(
                uri=row['uri'],
                prefLabel=row['prefLabel'],
                altLabel=row['altLabel'],
                exactMatch=row['exactMatch']
            )

    def _fetch_edmo_addresses(self):
        """Returns a generator of Institution objects, created from the latest
        export spreadsheet from seadatanet."""
        url = 'http://seadatanet.maris2.nl/v_edmo/browse_export.asp?order=&step=&count=0'
        # May need to use chardet to figure out correct encoding, but I think this is right:
        encoding = 'ISO-8859-1'
        use_enc = lambda s: smart_text(s, encoding=encoding)

        response = requests.get(url, stream=True)
        response.encoding = encoding
        response.raw.readline()  # "Export OF QUERY RESULTS FROM EDMO."
        # reader = csv.DictReader(open("edmo_export.csv"), skipinitialspace=True)
        reader = csv.DictReader(io.StringIO(response.text, newline=""), skipinitialspace=True)
        for row in reader:
            yield dict(URL=use_enc(row['URL']).strip(),
                       Name=use_enc(row['Name']).strip(),
                       Address=use_enc(row['Address']).strip(),
                       Address2=use_enc(row['Address 2']).strip(),
                       City=use_enc(row['City']).strip(),
                       State=use_enc(row['State']).strip(),
                       Zipcode=use_enc(row['Zipcode']).strip(),
                       Country=use_enc(row['Country']).strip())
