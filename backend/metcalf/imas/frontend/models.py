from django.db import models
from metcalf.common.models import AbstractSiteContent
from jsonfield import JSONField


class SiteContent(AbstractSiteContent):
    homepage_image = models.ImageField(
        upload_to='images', null=True,
        help_text="This is the image used on the homepage.  It needs to " \
                  "be high res so it looks good and it needs to be fairly dark " \
                  "so that the copy has good contrast and can be easily read. We "
                  "will optimise the size so it's not too heavy.")
    homepage_abstract = models.CharField(max_length=512, blank=True, null=False)
    code_location_name = models.CharField(max_length=32, blank=True, null=False)
    code_location_url = models.URLField(blank=True, null=False)
    homepage_acknowledgements = models.CharField(max_length=512, blank=True, null=False)


# Describes data sources (aka API endpoints)
class DataSource(models.Model):
    id = models.AutoField(primary_key=True)
    slug = models.SlugField(unique=True)
    base_url = models.CharField(max_length=200)
    search_param = models.CharField(max_length=32, default='query')
    response_key = models.CharField(max_length=128, default='results')
    schema = JSONField(blank=True, default={})

    def __str__(self):
        return "{0} #{1}".format(str(self.slug), self.pk)
