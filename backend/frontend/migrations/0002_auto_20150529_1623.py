# -*- coding: utf-8 -*-


from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='title',
            field=models.CharField(default=b'IMAS', max_length=32),
        ),
    ]
