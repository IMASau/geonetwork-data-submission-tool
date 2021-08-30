import uuid

from django.contrib.auth.models import User
from django.db import models
from django.urls import reverse
from django_fsm import FSMField
from jsonfield import JSONField
from lxml import etree

from metcalf.common.emails import *
from metcalf.common.utils import no_spaces_in_filename


class AbstractDataFeed(models.Model):
    IDLE = 'Idle'
    SCHEDULED = 'Scheduled'
    ACTIVE = 'Active'

    STATUS_CHOICES = (
        (IDLE, IDLE),
        (SCHEDULED, SCHEDULED),
        (ACTIVE, ACTIVE),
    )

    name = models.SlugField()

    state = FSMField(default=SCHEDULED, choices=STATUS_CHOICES)

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
    document = models.ForeignKey("Document", on_delete=models.CASCADE)
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    class Meta:
        abstract = True


class AbstractDocument(models.Model):
    DRAFT = 'Draft'
    SUBMITTED = 'Submitted'
    UPLOADED = 'Uploaded'
    ARCHIVED = 'Archived'
    DISCARDED = 'Discarded'

    STATUS_CHOICES = (
        (DRAFT, DRAFT),
        (SUBMITTED, SUBMITTED),
        (UPLOADED, UPLOADED),
        (ARCHIVED, ARCHIVED),
        (DISCARDED, DISCARDED),
    )

    uuid = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    template = models.ForeignKey("MetadataTemplate", on_delete=models.SET_NULL, null=True)
    title = models.TextField(default="Untitled")
    owner = models.ForeignKey(User, on_delete=models.CASCADE)
    status = FSMField(default=DRAFT, choices=STATUS_CHOICES)
    doi = models.CharField(max_length=1024, default='', blank=True)
    validation_result = models.TextField(null=True, blank=True, verbose_name="Validation result XML")
    validation_status = models.CharField(max_length=256, default='Unvalidated', null=True, blank=True,
                                         verbose_name='Validity')
    date_last_validated = models.DateTimeField(blank=True, null=True, verbose_name='Last Validated')

    # objects = DocumentManager()

    class Meta:
        abstract = True
        # FIXME is this needed
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
    document = models.ForeignKey("Document", on_delete=models.CASCADE, related_name='attachments')
    name = models.CharField(max_length=256)
    file = models.FileField(upload_to=no_spaces_in_filename)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    class Meta:
        abstract = True


class AbstractDraftMetadata(models.Model):
    document = models.ForeignKey("Document", on_delete=models.CASCADE)
    user = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)
    time = models.DateTimeField(auto_now_add=True)
    data = JSONField()
    noteForDataManager = models.TextField(default="")
    agreedToTerms = models.BooleanField(default=False)
    doiRequested = models.BooleanField(default=False)

    class Meta:
        # FIXME
        verbose_name_plural = "Draft Metadata"
        ordering = ["-time"]
        abstract = True


class AbstractMetadataTemplateMapper(models.Model):
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
    site = models.OneToOneField(Site, on_delete=models.SET_NULL, blank=True, null=True)
    title = models.CharField(max_length=32, default="Data Submission Tool")
    organisation_url = models.URLField(blank=True, null=True)
    tag_line = models.CharField(max_length=128, null=True, default="SHaRED Data Submission Tool")
    email = models.EmailField(default="esupport@tern.org.au")
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
    guide_pdf = models.URLField(null=True, verbose_name="Help", blank=True, max_length=1024)
    roadmap_pdf = models.URLField(null=True, verbose_name="Roadmap", blank=True, max_length=1024)
    releasenotes_url = models.URLField(null=True, verbose_name="Release notes URL", blank=True)
    portal_title = models.CharField(
        max_length=64, default="TERN Data Portal",
        help_text="Used to refer to the place where lodged data can be discovered")
    portal_url = models.URLField(blank=True, null=True, default='http://tern.org.au')
    portal_record_url = models.CharField(
        max_length=512,
        default="{{site.sitecontent.portal_url}}/edit/{{document.uuid}}",
        help_text="Used to generate URLs to the published record on the portal. "
                  "(Accepts django template formatting with access to `site` and `document`.)"
    )
    terms_pdf = models.URLField(blank=True, null=True, verbose_name='Terms PDF',
                                default='terms/TERN_Data_Provider_Deed_v1_9_DST.pdf')

    homepage_image_credit_name = models.CharField(max_length=128, default="XXX")
    homepage_image_credit_url = models.URLField(blank=True, null=True)

    class Meta:
        abstract = True
