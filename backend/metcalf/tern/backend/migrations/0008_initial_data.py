# Generated by Django 2.1.7 on 2020-01-27 22:13
from datetime import datetime

import pytz
from django.db import migrations


def configure_backend_data(apps, schema_editor):
    # We can't import the models directly as it may be a newer
    # version than this migration expects. We use the historical version.
    MetadataTemplateMapper = apps.get_model('backend', 'MetadataTemplateMapper')
    MetadataTemplate = apps.get_model('backend', 'MetadataTemplate')
    DataFeed = apps.get_model('backend', 'DataFeed')
    now = datetime.utcnow().replace(tzinfo=pytz.UTC)
    # Create default MetadataTemplateMapper
    mtm, _ = MetadataTemplateMapper.objects.get_or_create(pk='1')
    mtm.name = 'Tern Mapper'
    mtm.file = 'tern_template_spec.json'
    mtm.notes = 'Default template mapper'
    mtm.archived = 'f'
    mtm.site_id = '1'
    mtm.created = now
    mtm.modified = now
    mtm.save()
    # Create default MetadataTemplate
    mt, _ = MetadataTemplate.objects.get_or_create(pk='1')
    mt.name = "TERN Template"
    mt.file = "Metadata_Template.xml"
    mt.notes = "Default template"
    mt.archived = "f"
    mt.site_id = "1"
    mt.mapper_id = "1"
    mt.created = now
    mt.modified = now
    mt.save()
    # Create data feeds
    datafeeds = [
        "load_institutions", "load_horizontalresolutions", "load_parameterplatforms",
        "load_topiccategories", "load_sciencekeywords", "load_samplingfrequencies",
        "load_rolecodes", "load_persons", "load_parameterunits", "load_parameternames",
        "load_parameterinstruments"
    ]
    for idx, name in enumerate(datafeeds):
        df, _ = DataFeed.objects.get_or_create(pk=str(idx + 1))
        df.name = name
        df.state = "Scheduled"
        df.last_output = ""
        df.save()


class Migration(migrations.Migration):
    dependencies = [
        ('backend', '0007_auto_20200123_0123'),
        ('frontend', '0006_initial_data'),
    ]

    operations = [
        migrations.RunPython(configure_backend_data),
    ]