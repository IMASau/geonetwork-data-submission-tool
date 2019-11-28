# Generated by Django 2.1.7 on 2019-11-28 04:34

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('backend', '0003_auto_20191122_1156'),
    ]

    operations = [
        migrations.AddField(
            model_name='document',
            name='date_last_validated',
            field=models.DateTimeField(blank=True, null=True, verbose_name='Last Validated'),
        ),
        migrations.AddField(
            model_name='document',
            name='validation_result',
            field=models.TextField(blank=True, null=True, verbose_name='Validation result XML'),
        ),
        migrations.AddField(
            model_name='document',
            name='validation_status',
            field=models.CharField(blank=True, default='Unvalidated', max_length=256, null=True, verbose_name='Validity'),
        ),
    ]
