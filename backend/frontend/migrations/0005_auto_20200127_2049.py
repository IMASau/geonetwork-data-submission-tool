# Generated by Django 2.1.7 on 2020-01-27 20:49

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('frontend', '0004_auto_20200123_0123'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='homepage_image',
            field=models.CharField(blank=True, default='{{site.sitecontent.portal_url}}/static/img/Hemispherical-Photography.jpg', help_text='Url to homepage image. (Accepts django template formatting with access to `site` and `sitecontent`.)', max_length=200, null=True, verbose_name='Homepage Image'),
        ),
    ]