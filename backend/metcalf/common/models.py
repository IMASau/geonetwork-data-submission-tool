import uuid

from django.contrib.auth.models import User
from django.contrib.sites.models import Site
from django.db import models
from django.urls import reverse
from jsonfield import JSONField
from lxml import etree

from metcalf.common.utils import no_spaces_in_filename


class AbstractDataFeed(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.SlugField()

    last_refresh = models.DateTimeField(blank=True, null=True)
    last_success = models.DateTimeField(blank=True, null=True)
    last_failure = models.DateTimeField(blank=True, null=True)
    last_duration = models.DurationField(blank=True, null=True)

    last_output = models.TextField(blank=True)

    class Meta:
        abstract = True
        permissions = (
            ("datafeed_schedule", "Can schedule datafeed refresh"),
            ("datafeed_unschedule", "Can cancel scheduled datafeed schedule"),
            ("datafeed_admin", "Can administer datafeed"),
        )

    def feed_quality(self):
        if not self.last_refresh:
            return "Uninitialised"
        elif not self.last_success:
            return "Bad"
        elif not self.last_failure:
            return "Good"
        elif self.last_success > self.last_failure:
            return "Good"
        else:
            return "Stale"


class AbstractContributor(models.Model):
    id = models.AutoField(primary_key=True)
    document = models.ForeignKey("Document", on_delete=models.CASCADE, related_name="contributors")
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    class Meta:
        abstract = True


class AbstractDocument(models.Model):
    uuid = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    template = models.ForeignKey("MetadataTemplate", on_delete=models.SET_NULL, null=True)
    title = models.TextField(default="Untitled")
    owner = models.ForeignKey(User, on_delete=models.CASCADE)

    class Meta:
        abstract = True
        # FIXME: is this needed?
        permissions = (
            ("workflow_reject", "Can reject record in workflow"),
            ("workflow_upload", "Can upload record in workflow"),
            ("workflow_discard", "Can discard record in workflow"),
            ("workflow_restart", "Can restart record in workflow"),
            ("workflow_recover", "Can recover discarded records in workflow")
        )

    def short_title(self):
        return self.title[:32] + (self.title[32:] and '..')

    def add_creator(self, creators, person, nsmap):
        creator = etree.SubElement(creators, 'creator')
        creatorName = etree.SubElement(creator, 'creatorName')
        creatorName.text = '{last}, {first}'.format(last=person['familyName'], first=person['givenName'])

    ########################################################
    @property
    def latest_draft(self):
        all_drafts = self.draftmetadata_set.all()
        if all_drafts:
            return all_drafts[0]
        else:
            return None

    def __str__(self):
        return "{0} - {1} ({2})".format(str(self.uuid)[:8], self.short_title(), self.owner.email or self.owner.username)

    def is_editor(self, user):
        return user.is_staff or (user == self.owner)

    def get_absolute_url(self):
        return reverse('Edit', kwargs={'uuid': self.uuid})


class AbstractDocumentAttachment(models.Model):
    id = models.AutoField(primary_key=True)
    document = models.ForeignKey("Document", on_delete=models.CASCADE, related_name='attachments')
    name = models.CharField(max_length=256)
    file = models.FileField(upload_to=no_spaces_in_filename)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    class Meta:
        abstract = True


class AbstractDraftMetadata(models.Model):
    id = models.AutoField(primary_key=True)
    document = models.ForeignKey("Document", on_delete=models.CASCADE)
    user = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)
    time = models.DateTimeField(auto_now_add=True)
    data = JSONField()
    noteForDataManager = models.TextField(default="")

    class Meta:
        # FIXME
        verbose_name_plural = "Draft Metadata"
        ordering = ["-time"]
        abstract = True


class AbstractMetadataTemplateMapper(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=128, help_text="Unique name for template mapper.  Used in menus.")
    file = models.FileField("metadata_template_mappers",
                            help_text="JSON file used to interpret XML files that specify records")
    notes = models.TextField(help_text="Internal use notes about this template mapper")
    site = models.ForeignKey(Site, on_delete=models.SET_NULL, blank=True, null=True)
    archived = models.BooleanField(default=False)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    def __str__(self):
        return "{1} (#{0})".format(self.pk, self.name)

    class Meta:
        abstract = True


class AbstractMetadataTemplate(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=128, help_text="Unique name for template.  Used in menus.")
    file = models.FileField("metadata_templates", help_text="XML file used when creating and exporting records")
    notes = models.TextField(help_text="Internal use notes about this template")
    site = models.ForeignKey(Site, on_delete=models.SET_NULL, blank=True, null=True)
    archived = models.BooleanField(default=False)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)
    mapper = models.ForeignKey("MetadataTemplateMapper", on_delete=models.SET_NULL, blank=True, null=True)

    def __str__(self):
        return "{1} (#{0})".format(self.pk, self.name)

    class Meta:
        abstract = True


class AbstractSiteContent(models.Model):
    id = models.AutoField(primary_key=True)
    site = models.OneToOneField(Site, on_delete=models.SET_NULL, blank=True, null=True)
    title = models.CharField(max_length=32, default="Data Submission Tool")
    organisation_url = models.URLField(blank=True, null=True)
    tag_line = models.CharField(max_length=128, null=True)
    email = models.EmailField()
    homepage_image = models.CharField(
        max_length=200,
        blank=True, null=True, verbose_name='Homepage Image',
        help_text="Url to homepage image. (Accepts django template formatting with access to `site` and `sitecontent`.)")
    guide_pdf = models.URLField(null=True, verbose_name="Help", blank=True, max_length=1024)
    portal_title = models.CharField(
        max_length=64,
        help_text="Used to refer to the place where lodged data can be discovered")
    portal_url = models.URLField(blank=True, null=True)
    portal_record_url = models.CharField(
        max_length=512,
        default="{{site.sitecontent.portal_url}}/edit/{{document.uuid}}",
        help_text="Used to generate URLs to the published record on the portal. "
                  "(Accepts django template formatting with access to `site` and `document`.)"
    )

    homepage_image_credit_name = models.CharField(max_length=128, default="XXX")
    homepage_image_credit_url = models.URLField(blank=True, null=True)

    class Meta:
        abstract = True
