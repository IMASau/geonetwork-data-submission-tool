from django.contrib.sites.models import Site
from django.db import models
from imagekit.models import ProcessedImageField
from pilkit.processors import ResizeToFill


class SiteContent(models.Model):
    site = models.OneToOneField(Site, on_delete=models.SET_NULL, blank=True, null=True)
    title = models.CharField(max_length=32, default="TERN")
    organisation_url = models.URLField(blank=True, null=True)
    tag_line = models.CharField(max_length=128, default="Data Submission Tool")
    email = models.EmailField(default="imas.datamanager@utas.edu.au")
    homepage_image = ProcessedImageField(
        blank=True,
        null=True,
        upload_to='images',
        processors=[ResizeToFill(2048, 1024)],
        format='JPEG',
        options={'quality': 60},
        help_text="This is the image uses on the homepage.  It needs to " \
                  "be high res so it looks good and it needs to be fairly dark " \
                  "so that the copy has good contrast and can be easily read. We "
                  "will optimise the size so it's not too heavy.")
    guide_pdf = models.FileField(upload_to='guide', null=True)
    portal_title = models.CharField(
        max_length=64, default="TERN Data Portal",
        help_text="Used to refer to the place where lodged data can be discovered")
    portal_url = models.URLField(blank=True, null=True)
    portal_record_url = models.CharField(
        max_length=512,
        default="{{site.sitecontent.portal_url}}/portal/home?uuid={{document.uuid}}",
        help_text="Used to generate URLs to the published record on the portal. "
                  "(Accepts django template formatting with access to `site` and `document`.)"
    )
    terms_pdf = models.FileField(upload_to='terms', null=True)

    homepage_image_credit_name = models.CharField(max_length=128, default="XXX")
    homepage_image_credit_url = models.URLField(blank=True, null=True)