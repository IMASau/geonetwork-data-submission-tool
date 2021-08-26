import datetime
import logging

from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from lxml import etree
from urllib.request import urlopen

from metcalf.tern.backend.models import TopicCategory


logger = logging.getLogger(__name__)

class Command(BaseCommand):
    help = 'Refresh topic categories list from online'

    def add_arguments(self, parser):
        parser.add_argument('--admin-id',
                            dest='admin_id',
                            type=int,
                            help='Id to of user to run as (will default to first staff user)')

    def handle(self, *args, **options):
        adminpk = self._admin_pk(**options)

        try:
            with transaction.atomic():
                topic_categories = []
                with urlopen('http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml') as f:
                    xml = etree.parse(f)
                    namespaces = {'cat': 'http://standards.iso.org/iso/19115/-3/cat/1.0',
                                  'gco': 'http://standards.iso.org/iso/19115/-3/gco/1.0'}
                    results = xml.findall('.//cat:codelistItem/cat:CT_Codelist[@id="MD_TopicCategoryCode"]/cat:codeEntry/cat:CT_CodelistValue', namespaces)
                    for result in results:
                        identifier = result.findtext('.//cat:identifier/gco:ScopedName',default='Nope', namespaces=namespaces)
                        name = result.findtext('.//cat:name/gco:ScopedName',default='Nope',namespaces=namespaces)
                        topic_categories.append(TopicCategory(identifier=identifier,name=name))

                if not topic_categories:
                    raise CommandError('No role codes found, assuming error; aborting')

                TopicCategory.objects.all().delete()

                TopicCategory.objects.bulk_create(topic_categories)

                LogEntry.objects.log_action(
                    user_id=adminpk,
                    content_type_id=ContentType.objects.get_for_model(TopicCategory).pk,
                    object_id='',  # Hack; this disables the link in the admin log
                    object_repr=u'Refreshed topic category list - {:%Y-%m-%d %H:%M}'.format(datetime.datetime.utcnow()),
                    action_flag=CHANGE)
            logger.info("Finished loading {} topic categories".format(TopicCategory.objects.count()))
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
