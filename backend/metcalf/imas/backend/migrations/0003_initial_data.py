# Generated by Django 2.1.7 on 2020-01-27 22:13
from datetime import datetime

import pytz
from django.db import migrations


def configure_backend_data(apps, schema_editor):
    # We can't import the models directly as it may be a newer
    # version than this migration expects. We use the historical version.
    MetadataTemplateMapper = apps.get_model('backend', 'MetadataTemplateMapper')
    MetadataTemplate = apps.get_model('backend', 'MetadataTemplate')
    Site = apps.get_model('sites', 'Site')
    s, _ = Site.objects.get_or_create(pk='1')
    now = datetime.utcnow().replace(tzinfo=pytz.UTC)
    # Create default MetadataTemplateMapper
    mtm, _ = MetadataTemplateMapper.objects.get_or_create(pk='1')
    mtm.name = 'IMAS Mapper'
    mtm.file = 'imas_template_spec.json'
    mtm.notes = 'Default template mapper (WIP)'
    mtm.archived = 'f'
    mtm.site_id = '1'
    mtm.created = now
    mtm.modified = now
    mtm.save()
    # Create default MetadataTemplate
    mt, _ = MetadataTemplate.objects.get_or_create(pk='1')
    mt.name = "TERN Template"
    mt.file = "imas_metadata_template.xml"
    mt.notes = "Default template"
    mt.archived = "f"
    mt.site_id = "1"
    mt.mapper_id = "1"
    mt.created = now
    mt.modified = now
    mt.save()


class Migration(migrations.Migration):
    dependencies = [
        ('backend', '0002_auto_20210910_0700'),
        ('frontend', '0001_initial'),
    ]

    operations = [
        migrations.RunPython(configure_backend_data),
    ]
