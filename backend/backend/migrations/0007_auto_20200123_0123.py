# Generated by Django 2.1.7 on 2020-01-23 01:23

import django.db.models.deletion
from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ('backend', '0006_auto_20191211_1550'),
    ]

    operations = [
        migrations.AlterField(
            model_name='metadatatemplate',
            name='file',
            field=models.FileField(help_text='XML file used when creating and exporting records', upload_to='',
                                   verbose_name='metadata_templates'),
        ),
        migrations.AlterField(
            model_name='metadatatemplate',
            name='name',
            field=models.CharField(help_text='Unique name for template.  Used in menus.', max_length=128),
        ),
        migrations.AlterField(
            model_name='metadatatemplate',
            name='notes',
            field=models.TextField(help_text='Internal use notes about this template'),
        ),
        migrations.AlterField(
            model_name='metadatatemplate',
            name='site',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL,
                                    to='sites.Site'),
        ),
        migrations.AlterField(
            model_name='metadatatemplatemapper',
            name='file',
            field=models.FileField(help_text='JSON file used to interpret XML files that specify records', upload_to='',
                                   verbose_name='metadata_template_mappers'),
        ),
        migrations.AlterField(
            model_name='metadatatemplatemapper',
            name='name',
            field=models.CharField(help_text='Unique name for template mapper.  Used in menus.', max_length=128),
        ),
        migrations.AlterField(
            model_name='metadatatemplatemapper',
            name='notes',
            field=models.TextField(help_text='Internal use notes about this template mapper'),
        ),
        migrations.AlterField(
            model_name='metadatatemplatemapper',
            name='site',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL,
                                    to='sites.Site'),
        ),
        migrations.AlterField(
            model_name='person',
            name='electronicMailAddress',
            field=models.CharField(blank=True, default='', max_length=256, verbose_name='email'),
        ),
    ]
