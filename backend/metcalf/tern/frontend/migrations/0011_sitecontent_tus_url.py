# Generated by Django 3.2.7 on 2023-06-27 04:36

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0010_auto_20230615_0619'),
    ]

    operations = [
        migrations.AddField(
            model_name='sitecontent',
            name='tus_url',
            field=models.URLField(blank=True, null=True),
        ),
    ]