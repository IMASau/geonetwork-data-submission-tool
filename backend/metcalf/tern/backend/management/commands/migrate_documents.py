from django.contrib.admin.models import CHANGE, LogEntry
from django.contrib.auth.models import User
from django.contrib.contenttypes.models import ContentType
from django.core.exceptions import ObjectDoesNotExist
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
import requests
import json

from metcalf.tern.data_migration_tool.data_migration import migrate_data
from metcalf.tern.backend.models import Document, DraftMetadata, ScienceKeyword, AnzsrcKeyword

def keyword_to_label(keyword):
    keyword_values_in_array = [keyword.DetailedVariable,
                               keyword.VariableLevel3,
                               keyword.VariableLevel2,
                               keyword.VariableLevel1,
                               keyword.Term,
                               keyword.Topic,
                               keyword.Category]

    return next((x for x in keyword_values_in_array if x is not None and x != ""), "")


def keyword_to_breadcrumbs(keyword):
    keyword_values_in_array = [keyword.DetailedVariable,
                               keyword.VariableLevel3,
                               keyword.VariableLevel2,
                               keyword.VariableLevel1,
                               keyword.Term,
                               keyword.Topic,
                               keyword.Category]

    # Remove empty values.
    keyword_values_in_array = [x for x in keyword_values_in_array if x is not None and x != ""]

    # Remove first matching item (this will be the label)
    keyword_values_in_array = keyword_values_in_array[1:]

    # Reverse the breadcrumb order
    keyword_values_in_array.reverse()

    return [" | ".join(keyword_values_in_array)]

def gcmd_keywords_with_breadcrumb_info():
    keywords = ScienceKeyword.objects.all()
    return [{
        'label': keyword_to_label(k),
        'uri': k.uri,
        'breadcrumb': keyword_to_breadcrumbs(k)
    } for k in keywords]

def anzsrc_keywords_with_breadcrumb_info():
    keywords = AnzsrcKeyword.objects.all()
    return [{
        'label': keyword_to_label(k),
        'uri': k.uri,
        'breadcrumb': keyword_to_breadcrumbs(k)
    } for k in keywords]

def migrate_document(document, template, migrations):
    draft = document.latest_draft

    if draft:
        data = document.latest_draft.data
        new_data = migrate_data(data, template, migrations)

        # gcmd keywords
        all_keywords = gcmd_keywords_with_breadcrumb_info()
        old_keywords = new_data.get('identificationInfo', {}).get('keywordsTheme', {}).get('keywords')
        new_keywords = []

        if old_keywords:
            for old_keyword in old_keywords:
                new_keyword = next((k for k in all_keywords if (k.get('uri') == old_keyword.get('uri') and k.get('uri') != None)), old_keyword)
                new_keywords.append(new_keyword)
            
            if new_keywords:
                new_data['identificationInfo']['keywordsTheme'] = {}
                new_data['identificationInfo']['keywordsTheme']['keywords'] = new_keywords
        
        # anzsrc keywords
        all_keywords = anzsrc_keywords_with_breadcrumb_info()
        old_keywords = new_data.get('identificationInfo', {}).get('keywordsThemeAnzsrc', {}).get('keywords')
        new_keywords = []

        if old_keywords:
            for old_keyword in old_keywords:
                new_keyword = next((k for k in all_keywords if (k.get('uri') == old_keyword.get('uri') and k.get('uri') != None)), old_keyword)
                new_keywords.append(new_keyword)
            
            if new_keywords:
                new_data['identificationInfo']['keywordsThemeAnzsrc'] = {}
                new_data['identificationInfo']['keywordsThemeAnzsrc']['keywords'] = new_keywords
        
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