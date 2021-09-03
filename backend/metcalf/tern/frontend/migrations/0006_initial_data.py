# Generated by Django 2.1.7 on 2020-01-27 21:46

from django.db import migrations, models


def configure_site_content(apps, schema_editor):
    # We can't import the models directly as it may be a newer
    # version than this migration expects. We use the historical version.
    Site = apps.get_model('sites', 'Site')
    SiteContent = apps.get_model('frontend', 'SiteContent')
    # creati inital site
    site, _ = Site.objects.get_or_create(pk="1")
    site.domain = "shared.tern.org.au"
    site.name = "shared.tern.org.au"
    site.save()
    # site content
    sc, _ = SiteContent.objects.get_or_create(pk="1")
    sc.title = "TERN"
    sc.organisation_url = "https://shared.tern.org.au"
    sc.tag_line = "SHaRED Data Submission Tool"
    sc.email = "esupport@tern.org.au"
    sc.homepage_image = "{{site.sitecontent.portal_url}}/static/img/Hemispherical-Photography.jpg"
    sc.guide_pdf = "https://tern.org.au"
    sc.portal_title = "TERN Data Portal"
    sc.portal_url = "https://shared.tern.org.au"
    sc.portal_record_url = "{{site.sitecontent.portal_url}}/edit/{{document.uuid}}"
    sc.homepage_image_credit_name = "TERN Landscapes"
    sc.homepage_image_credit_url = "http://www.auscover.org.au/dataset_categories/vegetation-structural-properties-biomass/"
    sc.site = site
    sc.terms_pdf = "https://shared.tern.org.au/static/terms/TERN_Data_Provider_Deed_v1_9_DST.pdf"
    sc.doi_uri = "https://doi.tern.uq.edu.au/test/index.php?r=api/create&user_id=tern.data@uq.edu.au&app_id=aba241a4bad1c4f32f6e0a0266a2f3bf"
    sc.releasenotes_url = "https://ternaus.atlassian.net/wiki/spaces/SHaREDReleases/pages/705134733/SHaRED+Release+3.0"
    sc.roadmap_pdf = "https://shared.tern.org.au/media/guide/Coming_Soon_Roadmap.pdf"
    sc.save()


class Migration(migrations.Migration):
    dependencies = [
        ('frontend', '0005_auto_20200127_2049'),
    ]

    operations = [
        migrations.AlterField(
            model_name='sitecontent',
            name='terms_pdf',
            field=models.URLField(blank=True, default='terms/TERN_Data_Provider_Deed_v1_9_DST.pdf', null=True,
                                  verbose_name='Terms PDF'),
        ),
        migrations.RunPython(configure_site_content),
    ]
