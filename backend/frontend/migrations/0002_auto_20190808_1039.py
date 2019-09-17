# Generated by Django 2.1.7 on 2019-08-08 00:39

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='email',
            field=models.EmailField(default='imas.datamanager@utas.edu.au', max_length=254),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='portal_title',
            field=models.CharField(default='IMAS Data Portal', help_text='Used to refer to the place where lodged data can be discovered', max_length=64),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='title',
            field=models.CharField(default='IMAS', max_length=32),
        ),
    ]