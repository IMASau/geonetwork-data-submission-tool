# Generated by Django 2.1.7 on 2019-10-07 08:41

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0008_auto_20191007_1344'),
    ]

    operations = [
        migrations.AddField(
            model_name='sitecontent',
            name='releasenotes_url',
            field=models.URLField(null=True),
        ),
        migrations.AddField(
            model_name='sitecontent',
            name='roadmap_pdf',
            field=models.FileField(blank=True, null=True, upload_to='guide'),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='guide_pdf',
            field=models.FileField(blank=True, null=True, upload_to='guide'),
        ),
    ]
