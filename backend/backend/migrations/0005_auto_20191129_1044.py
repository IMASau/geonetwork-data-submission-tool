# Generated by Django 2.1.7 on 2019-11-28 23:44

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('backend', '0004_auto_20191128_1534'),
    ]

    operations = [
        migrations.AlterField(
            model_name='parametername',
            name='Definition',
            field=models.CharField(db_column='Definition', max_length=4096),
        ),
    ]