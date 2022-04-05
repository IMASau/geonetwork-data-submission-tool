from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
import requests
import json

from metcalf.tern.data_migration_tool.data_migration import migrate_data
from metcalf.tern.backend.models import Document, DraftMetadata

def migrate_document(document, template, migrations):
    draft = document.latest_draft

    if draft:
        data = document.latest_draft.data
        new_data = migrate_data(data, template, migrations)
        DraftMetadata.objects.create(
            document=document,
            user=document.owner,
            data=new_data
        )


class Command(BaseCommand):

    def add_arguments(self, parser):
        parser.add_argument(
            '--document',
            default=None
        )

    def handle(self, *args, **options):
        template_filepath = 'metcalf/tern/data_migration_tool/template.json'
        template = json.loads(open(template_filepath, 'r').read())

        migrations_filepath = 'metcalf/tern/data_migration_tool/migrations.json'
        migrations = json.loads(open(migrations_filepath, 'r').read())

        document_no = int(options['document']) if options['document'] != None else None

        if document_no == None:
            for document in Document.objects.all():
                migrate_document(document, template, migrations)
        else:
            migrate_document(Document.objects.all()[document_no], template, migrations)