from django.db import models

from metcalf.common.models import AbstractSiteContent


class SiteContent(AbstractSiteContent):
    tag_line = models.CharField(max_length=128, null=True, default="SHaRED Data Submission Tool")
    email = models.EmailField(default="esupport@tern.org.au")
    releasenotes_url = models.URLField(null=True, verbose_name="Release notes URL", blank=True)
    roadmap_pdf = models.URLField(null=True, verbose_name="Roadmap", blank=True, max_length=1024)
    doi_uri = models.CharField(
        max_length=1024,
        default='https://doi.tern.uq.edu.au/test/index.php?r=api/create&user_id=tern.data@uq.edu.au&app_id=',
        verbose_name="DOI Service URI",
        help_text="Base create URI for the DOI minting service")
    homepage_image = models.CharField(
        max_length=200,
        blank=True, null=True, verbose_name='Homepage Image',
        default='{{site.sitecontent.portal_url}}/static/img/Hemispherical-Photography.jpg',
        help_text="Url to homepage image. (Accepts django template formatting with access to `site` and `sitecontent`.)")
    portal_title = models.CharField(
        max_length=64, default="TERN Data Portal",
        help_text="Used to refer to the place where lodged data can be discovered")
    portal_url = models.URLField(blank=True, null=True, default='http://tern.org.au')
    terms_pdf = models.URLField(blank=True, null=True, verbose_name='Terms PDF',
                                default='terms/TERN_Data_Provider_Deed_v1_9_DST.pdf')
