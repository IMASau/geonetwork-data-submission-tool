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

from backend.models import HorizontalResolution


logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh horizontal resolutions list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                HorizontalResolution.objects.all().delete()

                src_url = 'https://gcmdservices.gsfc.nasa.gov/kms/concepts/concept_scheme/horizontalresolutionrange?format=xml'
                horizontal_resolutions = []
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
                            horizontal_resolutions.append(HorizontalResolution(uri=uri, prefLabel=prefLabel, prefLabelSortText=prefLabelSortText))
                if not horizontal_resolutions:
                    raise CommandError('No horizontal resolution definitions found, assuming error; aborting')
                HorizontalResolution.objects.bulk_create(horizontal_resolutions)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(HorizontalResolution).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed horizontal resolution list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} horizontal resolutions".format(HorizontalResolution.objects.count()))
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
