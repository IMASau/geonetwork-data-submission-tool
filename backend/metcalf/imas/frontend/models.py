from django.db import models
from metcalf.common.models import AbstractSiteContent


class SiteContent(AbstractSiteContent):
    homepage_image = models.ImageField(
        upload_to='images', null=True,
        help_text="This is the image uses on the homepage.  It needs to " \
                  "be high res so it looks good and it needs to be fairly dark " \
                  "so that the copy has good contrast and can be easily read. We "
                  "will optimise the size so it's not too heavy.")
