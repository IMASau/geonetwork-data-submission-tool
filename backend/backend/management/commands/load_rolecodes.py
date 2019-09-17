import csv
import datetime
import logging
from urllib.request import urlopen

import requests
from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from django.utils.encoding import smart_text

from lxml import etree

from backend.models import RoleCode

logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh role codes list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                RoleCode.objects.all().delete()

                print("Getting roles...")
                
                new_roles = self._fetch_rolecodes()
                if not new_roles:
                    raise CommandError('No role codes found, assuming error; aborting')
                RoleCode.objects.bulk_create(new_roles)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(RoleCode).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed role codes list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} role codes".format(RoleCode.objects.count()))
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

    
        
    def _fetch_rolecodes(self):
        """Returns a generator of RoleCode objects, created from the latest
        xml from isotc211."""
        url = 'https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml'
        
        # Retrieve file.
        document = urlopen(url)
        tree = etree.parse(document)
        
        # Define namespaces used by various elements
        base_namespace = "{http://www.isotc211.org/2005/gmx}"
        gml_namespace = "{http://www.opengis.net/gml/3.2}"
        
        role_code_sections = tree.findall("//" + base_namespace + "CodeListDictionary")
        
        # Find correct section
        correct_section = None
        for section in role_code_sections:
            if section.attrib[gml_namespace + "id"] == "CI_RoleCode":
                correct_section = section
        
        
        if correct_section is not None:
            for child in correct_section:
                if child.tag == base_namespace + "codeEntry":
                    
                    inner = child.find("" + base_namespace + "CodeDefinition")
                    desc = inner.find("" + gml_namespace + "description").text
                    identifier = inner.find("" + gml_namespace + "identifier").text
                    
                    yield RoleCode(Identifier=identifier,
                                   Description=desc)
        else:
            raise CommandError('Could not read xml file - no CodeListDectionary element with id "CI_RoleCode"')
















































