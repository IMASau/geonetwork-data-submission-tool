# Generated by Django 3.2.7 on 2021-11-18 10:12

from django.conf import settings
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('backend', '0016_document_hasuserdefined'),
    ]

    operations = [
        migrations.AddField(
            model_name='document',
            name='contributors',
            field=models.ManyToManyField(related_name='contributor', to=settings.AUTH_USER_MODEL),
        ),
    ]
