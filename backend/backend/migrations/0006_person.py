# Generated by Django 2.1.7 on 2019-08-21 04:39

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('backend', '0005_draftmetadata_agreedtoterms'),
    ]

    operations = [
        migrations.CreateModel(
            name='Person',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('uri', models.CharField(default='', max_length=512)),
                ('orgUri', models.CharField(default='', max_length=512)),
                ('familyName', models.CharField(max_length=256, verbose_name='family name')),
                ('givenName', models.CharField(max_length=256, verbose_name='given name')),
                ('honorificPrefix', models.CharField(max_length=256, verbose_name='honorific')),
            ],
        ),
    ]