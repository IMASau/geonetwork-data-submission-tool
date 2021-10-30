from django.db import migrations


def configure_backend_data(apps, schema_editor):
    # We can't import the models directly as it may be a newer
    # version than this migration expects. We use the historical version.
    Site = apps.get_model('sites', 'Site')
    s, new_s = Site.objects.get_or_create(pk='1')
    if new_s:
        s.domain = "localhost"
        s.name = "localhost"
        s.save()
    SiteContent = apps.get_model('frontend', 'SiteContent')
    sc, new_sc = SiteContent.objects.get_or_create(pk='1')
    if new_sc:
        sc.site_id = 1
        sc.email = 'test@example.com'
        sc.portal_title = 'IMAS Catalogue'
        sc.tag_line = 'Data Submission Tool'
        sc.homepage_image = 'IMAS04_KiW7x2D.jpeg'
        sc.save()


class Migration(migrations.Migration):
    dependencies = [
        ('frontend', '0004_auto_20211019_0110'),
    ]

    operations = [
        migrations.RunPython(configure_backend_data),
    ]
