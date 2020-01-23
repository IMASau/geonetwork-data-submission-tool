from django.contrib.sites.models import Site
from django.db import models
from imagekit.models import ProcessedImageField
from pilkit.processors import ResizeToFill


class SiteContent(models.Model):
    site = models.OneToOneField(Site, on_delete=models.SET_NULL, blank=True, null=True)
    title = models.CharField(max_length=32, default="TERN")
    organisation_url = models.URLField(blank=True, null=True)
    tag_line = models.CharField(max_length=128, default="SHaRED Data Submission Tool")
    email = models.EmailField(default="esupport@tern.org.au")
    doi_uri = models.CharField(max_length=1024,default='https://doi.tern.uq.edu.au/test/index.php?r=api/create&user_id=tern.data@uq.edu.au&app_id=',
                              verbose_name="DOI Service URI",
                              help_text="Base create URI for the DOI minting service")
    homepage_image = models.URLField(blank=True, null=True,verbose_name='Homepage Image',default='img/Hemispherical-Photography.jpg')
    guide_pdf = models.URLField(null=True,verbose_name="Help",blank=True, max_length=1024)
    roadmap_pdf = models.URLField(null=True,verbose_name="Roadmap",blank=True, max_length=1024)
    releasenotes_url = models.URLField(null=True,verbose_name="Release notes URL",blank=True)
    portal_title = models.CharField(
        max_length=64, default="TERN Data Portal",
        help_text="Used to refer to the place where lodged data can be discovered")
    portal_url = models.URLField(blank=True, null=True, default='http://tern.org.au')
    portal_record_url = models.CharField(
        max_length=512,
        default="{{site.sitecontent.portal_url}}/portal/home?uuid={{document.uuid}}",
        help_text="Used to generate URLs to the published record on the portal. "
                  "(Accepts django template formatting with access to `site` and `document`.)"
    )
    terms_pdf = models.URLField(blank=True, null=True,verbose_name='Homepage Image',default='terms/TERN_Data_Provider_Deed_v1_9_DST.pdf')

    homepage_image_credit_name = models.CharField(max_length=128, default="XXX")
    homepage_image_credit_url = models.URLField(blank=True, null=True)
