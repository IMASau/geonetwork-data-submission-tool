# Generated by Django 3.2.7 on 2021-10-18 08:48

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('backend', '0013_userinterfacetemplate'),
    ]

    operations = [
        migrations.AddField(
            model_name='metadatatemplate',
            name='ui_template',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='backend.userinterfacetemplate'),
        ),
    ]