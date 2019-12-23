# Generated by Django 2.1.7 on 2019-06-26 10:45

from django.db import migrations, models
import django.db.models.deletion
import imagekit.models.fields


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        ('sites', '0002_alter_domain_unique'),
    ]

    operations = [
        migrations.CreateModel(
            name='SiteContent',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('title', models.CharField(default='TERN', max_length=32)),
                ('organisation_url', models.URLField(blank=True, null=True)),
                ('tag_line', models.CharField(default='Data Submission Tool', max_length=128)),
                ('email', models.EmailField(default='esupport@tern.org.au', max_length=254)),
                ('homepage_image', imagekit.models.fields.ProcessedImageField(blank=True, help_text="This is the image used on the homepage.  It needs to be high res so it looks good and it needs to be fairly dark so that the copy has good contrast and can be easily read. We will optimise the size so it's not too heavy.", null=True, upload_to='images')),
                ('guide_pdf', models.URLField(blank=True, default='http://tern.org.au', null=True, max_length=1024)),
                ('portal_title', models.CharField(default='TERN Data Portal', help_text='Used to refer to the place where lodged data can be discovered', max_length=64)),
                ('portal_url', models.URLField(blank=True, null=True)),
                ('portal_record_url', models.CharField(default='{{site.sitecontent.portal_url}}/portal/home?uuid={{document.uuid}}', help_text='Used to generate URLs to the published record on the portal. (Accepts django template formatting with access to `site` and `document`.)', max_length=512)),
                ('homepage_image_credit_name', models.CharField(default='XXX', max_length=128)),
                ('homepage_image_credit_url', models.URLField(blank=True, null=True)),
                ('site', models.OneToOneField(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='sites.Site')),
                ('terms_pdf', models.FileField(null=True, upload_to='terms')),
                ('doi_uri', models.CharField(default='YOUR-DOI-LINK', max_length=1024)),
                ('releasenotes_url', models.URLField(blank=True, null=True, verbose_name='Release notes URL')),
                ('roadmap_pdf', models.URLField(blank=True, default='http://tern.org.au', null=True, max_length=1024)),
            ],
        ),
    ]
