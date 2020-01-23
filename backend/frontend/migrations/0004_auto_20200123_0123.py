# Generated by Django 2.1.7 on 2020-01-23 01:23

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0003_auto_20191129_1050'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='homepage_image',
            field=models.URLField(blank=True, default='img/Hemispherical-Photography.jpg', null=True, verbose_name='Homepage Image'),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='homepage_image_credit_name',
            field=models.CharField(default='XXX', max_length=128),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='homepage_image_credit_url',
            field=models.URLField(blank=True, null=True),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='releasenotes_url',
            field=models.URLField(blank=True, null=True, verbose_name='Release notes URL'),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='roadmap_pdf',
            field=models.URLField(blank=True, max_length=1024, null=True, verbose_name='Roadmap'),
        ),
        migrations.AlterField(
            model_name='sitecontent',
            name='terms_pdf',
            field=models.URLField(blank=True, default='terms/TERN_Data_Provider_Deed_v1_9_DST.pdf', null=True, verbose_name='Homepage Image'),
        ),
    ]
