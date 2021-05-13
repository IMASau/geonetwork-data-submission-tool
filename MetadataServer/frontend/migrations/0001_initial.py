# -*- coding: utf-8 -*-


from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('sites', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='SiteContent',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('title', models.CharField(default=b'IMAS', max_length=16)),
                ('organisation_url', models.URLField(null=True, blank=True)),
                ('tag_line', models.CharField(default=b'Data Submission Tool', max_length=128)),
                ('email', models.EmailField(default=b'imas.datamanager@utas.edu.au', max_length=254)),
                ('homepage_image', models.ImageField(help_text=b"This is the image uses on the homepage.  It needs to be high res so it looks good and it needs to be fairly dark so that the copy has good contrast and can be easily read. We will optimise the size so it's not too heavy.", null=True, upload_to=b'images')),
                ('guide_pdf', models.FileField(null=True, upload_to=b'guide')),
                ('portal_title', models.CharField(default=b'IMAS Data Portal', help_text=b'Used to refer to the place where lodged data can be discovered', max_length=64)),
                ('portal_url', models.URLField(null=True, blank=True)),
                ('portal_record_url', models.CharField(default=b'{{site.sitecontent.portal_url}}/portal/home?uuid={{document.uuid}}', help_text=b'Used to generate URLs to the published record on the portal. (Accepts django template formatting with access to `site` and `document`.)', max_length=512)),
                ('homepage_image_credit_name', models.CharField(default=b'XXX', max_length=128)),
                ('homepage_image_credit_url', models.URLField(null=True, blank=True)),
                ('site', models.OneToOneField(to='sites.Site')),
            ],
        ),
    ]
