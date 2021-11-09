# Generated by Django 3.2.7 on 2021-10-18 05:04

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('sites', '0002_alter_domain_unique'),
        ('backend', '0011_auto_20211018_0503'),
    ]

    operations = [
        migrations.AlterField(
            model_name='contributor',
            name='document',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='contributors', to='backend.document'),
        ),
    ]