# Generated by Django 2.1.7 on 2020-08-24 05:44

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0006_initial_data'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='title',
            field=models.CharField(default='Data Submission Tool', max_length=32),
        ),
    ]