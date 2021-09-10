# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import jsonfield.fields
from django.conf import settings
import django_fsm
import uuid


class Migration(migrations.Migration):

    dependencies = [
        ('sites', '0001_initial'),
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Contributor',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
            ],
        ),
        migrations.CreateModel(
            name='Document',
            fields=[
                ('uuid', models.UUIDField(default=uuid.uuid4, serialize=False, editable=False, primary_key=True)),
                ('title', models.TextField(default=b'Untitled')),
                ('status', django_fsm.FSMField(default=b'Draft', max_length=50, choices=[(b'Draft', b'Draft'), (b'Submitted', b'Submitted'), (b'Uploaded', b'Uploaded'), (b'Archived', b'Archived'), (b'Discarded', b'Discarded')])),
                ('owner', models.ForeignKey(to=settings.AUTH_USER_MODEL, on_delete=models.CASCADE)),
            ],
            options={
                'permissions': (('workflow_reject', 'Can reject record in workflow'), ('workflow_upload', 'Can upload record in workflow'), ('workflow_discard', 'Can discard record in workflow'), ('workflow_restart', 'Can restart record in workflow'), ('workflow_recover', 'Can recover discarded records in workflow')),
            },
        ),
        migrations.CreateModel(
            name='DocumentAttachment',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(max_length=256)),
                ('file', models.FileField(upload_to=b'')),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('modified', models.DateTimeField(auto_now=True)),
                ('document', models.ForeignKey(related_name='attachments', to='backend.Document', on_delete=models.CASCADE)),
            ],
        ),
        migrations.CreateModel(
            name='DraftMetadata',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('time', models.DateTimeField(auto_now_add=True)),
                ('data', jsonfield.fields.JSONField()),
                ('document', models.ForeignKey(to='backend.Document', on_delete=models.CASCADE)),
                ('user', models.ForeignKey(to=settings.AUTH_USER_MODEL, null=True, on_delete=models.CASCADE)),
            ],
            options={
                'ordering': ['-time'],
                'verbose_name_plural': 'Draft Metadata',
            },
        ),
        migrations.CreateModel(
            name='Institution',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('organisationName', models.CharField(max_length=256, verbose_name=b'organisation name')),
                ('deliveryPoint', models.CharField(max_length=256, verbose_name=b'street address')),
                ('deliveryPoint2', models.CharField(max_length=256, verbose_name=b'street address 2')),
                ('city', models.CharField(max_length=128)),
                ('administrativeArea', models.CharField(max_length=64, verbose_name=b'state')),
                ('postalCode', models.CharField(max_length=16, verbose_name=b'postcode')),
                ('country', models.CharField(max_length=64)),
            ],
        ),
        migrations.CreateModel(
            name='MetadataTemplate',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(help_text=b'Unique name for template.  Used in menus.', max_length=128)),
                ('file', models.FileField(help_text=b'XML file used when creating and exporting records', upload_to=b'', verbose_name=b'metadata_templates')),
                ('notes', models.TextField(help_text=b'Internal use notes about this template')),
                ('archived', models.BooleanField(default=False)),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('modified', models.DateTimeField(auto_now=True)),
                ('site', models.ForeignKey(blank=True, to='sites.Site', null=True, on_delete=models.CASCADE)),
            ],
        ),
        migrations.CreateModel(
            name='ScienceKeyword',
            fields=[
                ('UUID', models.UUIDField(default=uuid.uuid4, serialize=False, editable=False, primary_key=True)),
                ('Category', models.CharField(max_length=128)),
                ('Topic', models.CharField(max_length=128)),
                ('Term', models.CharField(max_length=128)),
                ('VariableLevel1', models.CharField(max_length=128)),
                ('VariableLevel2', models.CharField(max_length=128)),
                ('VariableLevel3', models.CharField(max_length=128)),
                ('DetailedVariable', models.CharField(max_length=128)),
            ],
            options={
                'ordering': ['Category', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3', 'DetailedVariable'],
            },
        ),
        migrations.AddField(
            model_name='document',
            name='template',
            field=models.ForeignKey(to='backend.MetadataTemplate', null=True, on_delete=models.CASCADE),
        ),
        migrations.AddField(
            model_name='contributor',
            name='document',
            field=models.ForeignKey(to='backend.Document', on_delete=models.CASCADE),
        ),
        migrations.AddField(
            model_name='contributor',
            name='user',
            field=models.ForeignKey(to=settings.AUTH_USER_MODEL, on_delete=models.CASCADE),
        ),
    ]
