# Generated by Django 2.1.7 on 2019-10-08 05:58

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0009_auto_20191007_1941'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='releasenotes_url',
            field=models.URLField(null=True, verbose_name='Release notes URL'),
        ),
    ]