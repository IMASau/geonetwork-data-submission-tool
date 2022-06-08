# Generated by Django 3.2.7 on 2022-05-26 02:57

from django.conf import settings
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('backend', '0009_auto_20220404_0731'),
    ]

    operations = [
        migrations.AlterField(
            model_name='document',
            name='contributors',
            field=models.ManyToManyField(blank=True, related_name='contributor', to=settings.AUTH_USER_MODEL),
        ),
    ]