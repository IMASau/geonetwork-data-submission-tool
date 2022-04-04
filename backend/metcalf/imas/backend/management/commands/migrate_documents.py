from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
import requests
import json

from metcalf.imas.data_migration_tool.data_migration import migrate_data
from metcalf.imas.backend.models import Document, DraftMetadata, DocumentAttachment

def migrate_document(document, template, migrations):
    data = document.latest_draft.data
    data = json.loads(data) if isinstance(data, str) else data
    new_data = migrate_data(data, template, migrations)

    if not new_data.get('attachments'):
        attachments = []

        # this code could probably be sped up
        for attachment in DocumentAttachment.objects.all():
            if attachment.document == document:
                attachments.append({
                    'id': attachment.id,
                    'file': f'{attachment.file}',
                    'name': attachment.name,
                    'delete_url': f'/delete/{new_data.get("fileIdentifier")}/{attachment.id}',
                    'created': attachment.created,
                    'modified': attachment.modified
                })
        
        if len(attachments) > 0:
            new_data['attachments'] = attachments

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
        template_filepath = 'metcalf/imas/data_migration_tool/template.json'
        template = json.loads(open(template_filepath, 'r').read())

        migrations_filepath = 'metcalf/imas/data_migration_tool/migrations.json'
        migrations = json.loads(open(migrations_filepath, 'r').read())

        document_no = int(options['document']) if options['document'] != None else None

        if document_no == None:
            for document in Document.objects.all():
                migrate_document(document, template, migrations)
        else:
            migrate_document(Document.objects.all()[document_no], template, migrations)