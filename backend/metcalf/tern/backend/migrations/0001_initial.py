# Generated by Django 2.1.7 on 2019-06-26 10:15

import django.db.models.deletion
import django_fsm
import jsonfield.fields
import uuid
from django.conf import settings
from django.db import migrations, models

from metcalf.common.utils import no_spaces_in_filename


class Migration(migrations.Migration):
    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('sites', '0002_alter_domain_unique'),
    ]

    operations = [
        migrations.CreateModel(
            name='MetadataTemplateMapper',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(help_text='Unique name for template mapper.  Used in menus.', max_length=128,
                                          default='TERN Mapper')),
                ('file',
                 models.FileField(help_text='JSON file used to interpret XML files that specify records', upload_to='',
                                  verbose_name='metadata_template_mappers', default='tern_template_spec.json')),
                ('notes', models.TextField(help_text='Internal use notes about this template mapper',
                                           default='Default template mapper')),
                ('archived', models.BooleanField(default=False)),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('modified', models.DateTimeField(auto_now=True)),
                ('site',
                 models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='sites.Site',
                                   default=1)),
            ],
        ),
        migrations.CreateModel(
            name='MetadataTemplate',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(help_text='Unique name for template.  Used in menus.', max_length=128,
                                          default='TERN Template')),
                ('file', models.FileField(help_text='XML file used when creating and exporting records', upload_to='',
                                          verbose_name='metadata_templates', default='Metadata_Template.xml')),
                ('notes',
                 models.TextField(help_text='Internal use notes about this template', default='Default template')),
                ('archived', models.BooleanField(default=False)),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('modified', models.DateTimeField(auto_now=True)),
                ('site',
                 models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='sites.Site',
                                   default=1)),
                ('mapper', models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL,
                                             to='backend.MetadataTemplateMapper')),
            ],
        ),
        migrations.CreateModel(
            name='Document',
            fields=[
                ('uuid', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('title', models.TextField(default='Untitled')),
                ('status', django_fsm.FSMField(
                    choices=[('Draft', 'Draft'), ('Submitted', 'Submitted'), ('Uploaded', 'Uploaded'),
                             ('Archived', 'Archived'), ('Discarded', 'Discarded')], default='Draft', max_length=50)),
                ('owner', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
                ('doi', models.CharField(default='', null=True, max_length=1024)),
                ('template', models.ForeignKey(null=True, on_delete=django.db.models.deletion.SET_NULL,
                                               to='backend.MetadataTemplate')),
            ],
            options={
                'permissions': (('workflow_reject', 'Can reject record in workflow'),
                                ('workflow_upload', 'Can upload record in workflow'),
                                ('workflow_discard', 'Can discard record in workflow'),
                                ('workflow_restart', 'Can restart record in workflow'),
                                ('workflow_recover', 'Can recover discarded records in workflow')),
            },
        ),
        migrations.CreateModel(
            name='Contributor',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('document', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='backend.Document')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.CreateModel(
            name='DataFeed',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.SlugField()),
                ('state',
                 django_fsm.FSMField(choices=[('Idle', 'Idle'), ('Scheduled', 'Scheduled'), ('Active', 'Active')],
                                     default='Scheduled', max_length=50)),
                ('last_refresh', models.DateTimeField(blank=True, null=True)),
                ('last_success', models.DateTimeField(blank=True, null=True)),
                ('last_failure', models.DateTimeField(blank=True, null=True)),
                ('last_duration', models.DurationField(blank=True, null=True)),
                ('last_output', models.TextField(blank=True)),
            ],
            options={
                'permissions': (('datafeed_schedule', 'Can schedule datafeed refresh'),
                                ('datafeed_unschedule', 'Can cancel scheduled datafeed schedule'),
                                ('datafeed_admin', 'Can administer datafeed')),
            },
        ),
        migrations.CreateModel(
            name='DocumentAttachment',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=256)),
                ('file', models.FileField(upload_to=no_spaces_in_filename)),
                ('created', models.DateTimeField(auto_now_add=True)),
                ('modified', models.DateTimeField(auto_now=True)),
                ('document', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='attachments',
                                               to='backend.Document')),
            ],
        ),
        migrations.CreateModel(
            name='DraftMetadata',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('time', models.DateTimeField(auto_now_add=True)),
                ('data', jsonfield.fields.JSONField()),
                ('noteForDataManager', models.TextField(default='')),
                ('document', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='backend.Document')),
                ('user', models.ForeignKey(null=True, on_delete=django.db.models.deletion.SET_NULL,
                                           to=settings.AUTH_USER_MODEL)),
                ('doiRequested', models.BooleanField(default=False)),
                ('agreedToTerms', models.BooleanField(default=False)),
            ],
            options={
                'verbose_name_plural': 'Draft Metadata',
                'ordering': ['-time'],
            },
        ),
        migrations.CreateModel(
            name='Institution',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('uri', models.CharField(default='', max_length=512)),
                ('prefLabel', models.CharField(default='', max_length=512)),
                ('altLabel', models.CharField(default='', max_length=512)),
                ('exactMatch', models.CharField(default='', max_length=512)),
                ('organisationName', models.CharField(max_length=256, verbose_name='organisation name')),
                ('deliveryPoint', models.CharField(max_length=256, verbose_name='street address')),
                ('deliveryPoint2', models.CharField(max_length=256, verbose_name='street address 2')),
                ('city', models.CharField(max_length=128)),
                ('administrativeArea', models.CharField(max_length=64, verbose_name='state')),
                ('postalCode', models.CharField(max_length=64, verbose_name='postcode')),
                ('country', models.CharField(max_length=64)),
                ('isUserAdded', models.BooleanField(default=False, verbose_name='User Added')),
            ],
        ),
        migrations.CreateModel(
            name='ParameterInstrument',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('lft', models.PositiveIntegerField(db_index=True)),
                ('rgt', models.PositiveIntegerField(db_index=True)),
                ('tree_id', models.PositiveIntegerField(db_index=True)),
                ('depth', models.PositiveIntegerField(db_index=True)),
                ('URI', models.CharField(db_column='URI', max_length=128)),
                ('Name', models.CharField(db_column='Name', max_length=128)),
                ('Definition', models.CharField(db_column='Definition', max_length=2500)),
                ('is_selectable', models.BooleanField()),
                ('Version', models.CharField(db_column='Version', max_length=16)),
            ],
            options={
                'ordering': ['Name'],
            },
        ),
        migrations.CreateModel(
            name='ParameterName',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('lft', models.PositiveIntegerField(db_index=True)),
                ('rgt', models.PositiveIntegerField(db_index=True)),
                ('tree_id', models.PositiveIntegerField(db_index=True)),
                ('depth', models.PositiveIntegerField(db_index=True)),
                ('URI', models.CharField(db_column='URI', max_length=128)),
                ('Name', models.CharField(db_column='Name', max_length=128)),
                ('Definition', models.CharField(db_column='Definition', max_length=1024)),
                ('is_selectable', models.BooleanField()),
                ('Version', models.CharField(db_column='Version', max_length=16)),
            ],
            options={
                'ordering': ['Name'],
            },
        ),
        migrations.CreateModel(
            name='ParameterPlatform',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('lft', models.PositiveIntegerField(db_index=True)),
                ('rgt', models.PositiveIntegerField(db_index=True)),
                ('tree_id', models.PositiveIntegerField(db_index=True)),
                ('depth', models.PositiveIntegerField(db_index=True)),
                ('URI', models.CharField(db_column='URI', max_length=128)),
                ('Name', models.CharField(db_column='Name', max_length=128)),
                ('Definition', models.CharField(db_column='Definition', max_length=5000)),
                ('is_selectable', models.BooleanField()),
                ('Version', models.CharField(db_column='Version', max_length=16)),
            ],
            options={
                'ordering': ['Name'],
            },
        ),
        migrations.CreateModel(
            name='ParameterUnit',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('lft', models.PositiveIntegerField(db_index=True)),
                ('rgt', models.PositiveIntegerField(db_index=True)),
                ('tree_id', models.PositiveIntegerField(db_index=True)),
                ('depth', models.PositiveIntegerField(db_index=True)),
                ('URI', models.CharField(db_column='URI', max_length=128)),
                ('Name', models.CharField(db_column='Name', max_length=128)),
                ('Definition', models.CharField(db_column='Definition', max_length=256)),
                ('is_selectable', models.BooleanField()),
                ('Version', models.CharField(db_column='Version', max_length=16)),
            ],
            options={
                'ordering': ['Name'],
            },
        ),
        migrations.CreateModel(
            name='RoleCode',
            fields=[
                ('UUID', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('Identifier', models.CharField(max_length=128)),
                ('Description', models.CharField(max_length=256)),
            ],
            options={
                'ordering': ['Identifier', 'Description'],
            },
        ),
        migrations.CreateModel(
            name='ScienceKeyword',
            fields=[
                ('UUID', models.UUIDField(default=uuid.uuid4, editable=False, primary_key=True, serialize=False)),
                ('Category', models.CharField(max_length=128)),
                ('Topic', models.CharField(max_length=128)),
                ('Term', models.CharField(max_length=128)),
                ('VariableLevel1', models.CharField(max_length=128)),
                ('VariableLevel2', models.CharField(max_length=128)),
                ('VariableLevel3', models.CharField(max_length=128)),
                ('DetailedVariable', models.CharField(max_length=128)),
            ],
            options={
                'ordering': ['Category', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                             'DetailedVariable'],
            },
        ),

        migrations.CreateModel(
            name='Person',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('uri', models.CharField(default='', max_length=512)),
                ('orgUri', models.CharField(default='', max_length=512)),
                ('familyName', models.CharField(max_length=256, verbose_name='family name')),
                ('givenName', models.CharField(max_length=256, verbose_name='given name')),
                ('honorificPrefix', models.CharField(blank=True, max_length=256, verbose_name='honorific')),
                ('isUserAdded', models.BooleanField(default=False, verbose_name='User Added')),
                ('prefLabel', models.CharField(default='', max_length=512)),
                ('electronicMailAddress', models.CharField(default='', max_length=256, verbose_name='email')),
                ('orcid', models.CharField(blank=True, default='', max_length=50, verbose_name='ORCID ID')),
            ],
            options={
                'ordering': ['familyName', 'givenName'],
            },
        ),
        migrations.CreateModel(
            name='SamplingFrequency',
            fields=[
                ('uri', models.CharField(default='', max_length=512, primary_key=True, serialize=False)),
                ('prefLabel', models.CharField(max_length=256)),
                ('prefLabelSortText', models.CharField(default='', max_length=256)),
            ],
        ),
        migrations.CreateModel(
            name='TopicCategory',
            fields=[
                ('identifier', models.CharField(max_length=256, primary_key=True, serialize=False)),
                ('name', models.CharField(max_length=256)),
            ],
            options={
                'ordering': ['identifier'],
            },
        ),
        migrations.CreateModel(
            name='AnzsrcKeyword',
            fields=[
                ('UUID', models.CharField(default='', editable=False, max_length=256, primary_key=True, serialize=False,
                                          verbose_name='URL')),
                ('Category', models.CharField(max_length=128)),
                ('Topic', models.CharField(max_length=128)),
                ('Term', models.CharField(max_length=128)),
                ('VariableLevel1', models.CharField(max_length=128)),
                ('VariableLevel2', models.CharField(max_length=128)),
                ('VariableLevel3', models.CharField(max_length=128)),
                ('DetailedVariable', models.CharField(max_length=128)),
            ],
            options={
                'ordering': ['Category', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                             'DetailedVariable'],
            },
        ),
        migrations.CreateModel(
            name='HorizontalResolution',
            fields=[
                ('uri', models.CharField(default='', max_length=512, primary_key=True, serialize=False)),
                ('prefLabel', models.CharField(max_length=256)),
                ('prefLabelSortText', models.CharField(default='', max_length=256)),
            ],
        ),
    ]
