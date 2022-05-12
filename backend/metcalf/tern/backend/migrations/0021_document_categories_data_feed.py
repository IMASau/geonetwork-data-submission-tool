# Generated by Django 3.2.7 on 2022-05-02 01:44

from django.db import migrations


def add_category(apps, schema_editor):
    DataFeed = apps.get_model('backend', 'DataFeed')
    df_name = "load_document_categories"
    df, _ = DataFeed.objects.get_or_create(name=df_name)

    df.name = df_name
    df.state = "Scheduled"
    df.last_output = ""
    df.save()


class Migration(migrations.Migration):

    dependencies = [
        ('backend', '0020_auto_20220401_0508'),
    ]

    operations = [
        migrations.RunPython(add_category),
    ]