import uuid
import copy
import json

from django.contrib.auth.models import User
from django.core.urlresolvers import reverse
from django.db import models
from jsonfield import JSONField
from lxml import etree
from django.core.exceptions import ValidationError

from rest_framework.renderers import JSONRenderer

from django_fsm import FSMField, transition

from backend.utils import to_json
from backend.xmlutils import extract_xml_data, data_to_xml, extract_fields
from backend.emails import *
from backend.spec_2_0 import make_spec


class MetadataTemplate(models.Model):
    name = models.CharField(max_length=128, help_text="Unique name for template.  Used in menus.")
    file = models.FileField("metadata_templates", help_text="XML file used when creating and exporting records")
    notes = models.TextField(help_text="Internal use notes about this template")
    site = models.ForeignKey(Site, blank=True, null=True)
    archived = models.BooleanField(default=False)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    def clean(self):
        try:
            tree = etree.fromstring(self.file.read())
            spec = make_spec(science_keyword=ScienceKeyword)
            fields = extract_fields(tree, spec)
            data = extract_xml_data(tree, spec)
            # FIXME data_to_xml will validate presence of all nodes in the template, but only when data is fully mocked up
            data = json.loads(JSONRenderer().render(data))
            data_to_xml(data, tree, spec, spec['namespaces'], silent=False)
        except Exception as e:
            raise ValidationError({'file': e.message})

    def __unicode__(self):
        return u"{1} (#{0})".format(self.pk, self.name)


class DocumentManager(models.Manager):
    def clone(self, orig_doc, user):
        new_title = orig_doc.title + " (Clone)"

        doc = Document.objects.create(
            title=new_title,
            template=orig_doc.template,
            owner=user)

        new_data = copy.deepcopy(to_json(orig_doc.latest_draft.data))
        new_data['identificationInfo']['title'] = new_title
        new_data['fileIdentifier'] = doc.pk

        DraftMetadata.objects.create(
            document=doc,
            user=user,
            data=new_data)

        return doc


class Document(models.Model):
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
    template = models.ForeignKey(MetadataTemplate, null=True)
    title = models.TextField(default="Untitled")
    owner = models.ForeignKey(User)
    status = FSMField(default=DRAFT, choices=STATUS_CHOICES)

    objects = DocumentManager()

    class Meta:
        permissions = (
            ("workflow_reject", "Can reject record in workflow"),
            ("workflow_upload", "Can upload record in workflow"),
            ("workflow_discard", "Can discard record in workflow"),
            ("workflow_restart", "Can restart record in workflow"),
            ("workflow_recover", "Can recover discarded records in workflow")
        )

    def short_title(self):
        return self.title[:32] + (self.title[32:] and '..')

    ########################################################
    # Workflow (state) Transitions
    @transition(field=status, source=[DRAFT, SUBMITTED], target=ARCHIVED)
    def archive(self):
        pass

    @transition(field=status, source=[ARCHIVED], target=DRAFT)
    def restore(self):
        pass

    @transition(field=status, source=SUBMITTED, target=DRAFT, permission='backend.workflow_reject')
    def reject(self):
        pass

    @transition(field=status, source=DRAFT, target=SUBMITTED)
    def submit(self):
        email_user_submit_confirmation(self)
        email_manager_submit_alert(self)

    @transition(field=status, source=SUBMITTED, target=SUBMITTED)
    def resubmit(self):
        email_manager_updated_alert(self)

    @transition(field=status, source=SUBMITTED, target=UPLOADED, permission='backend.workflow_upload')
    def upload(self):
        email_user_upload_alert(self)

    @transition(field=status, source=[UPLOADED], target=DISCARDED, permission='backend.workflow_discard')
    def discard(self):
        pass

    @transition(field=status, source=[DISCARDED], target=ARCHIVED, permission='backend.workflow_recover')
    def recover(self):
        pass

    @transition(field=status, source=[ARCHIVED], target=DISCARDED)
    def delete_archived(self):
        pass

    @transition(field=status, source=[UPLOADED], target=DRAFT, permission='backend.workflow_restart')
    def restart(self):
        pass

    ########################################################
    @property
    def latest_draft(self):
        return self.draftmetadata_set.all()[0]

    def __unicode__(self):
        return u"{0} - {1} ({2})".format(str(self.uuid)[:8], self.short_title(), self.owner.username)

    def is_editor(self, user):
        return user.is_staff or (user == self.owner)

    def get_absolute_url(self):
        return reverse('Edit', kwargs={'uuid': self.uuid})


class Contributor(models.Model):
    document = models.ForeignKey("Document")
    user = models.ForeignKey(User)


class DraftMetadata(models.Model):
    document = models.ForeignKey("Document")
    user = models.ForeignKey(User, null=True)
    time = models.DateTimeField(auto_now_add=True)
    data = JSONField()

    class Meta:
        verbose_name_plural = "Draft Metadata"
        ordering = ["-time"]


class DocumentAttachment(models.Model):
    document = models.ForeignKey("Document", related_name='attachments')
    name = models.CharField(max_length=256)
    file = models.FileField()
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    class Meta:
        pass


class Institution(models.Model):
    organisationName = models.CharField(max_length=256, verbose_name="organisation name")
    deliveryPoint = models.CharField(max_length=256, verbose_name="street address")
    deliveryPoint2 = models.CharField(max_length=256, verbose_name="street address 2")
    city = models.CharField(max_length=128)
    administrativeArea = models.CharField(max_length=64, verbose_name="state")
    postalCode = models.CharField(max_length=16, verbose_name="postcode")
    country = models.CharField(max_length=64)

    def to_dict(self):
        return dict(
            organisationName=self.organisationName,
            deliveryPoint=self.deliveryPoint,
            deliveryPoint2=self.deliveryPoint2,
            city=self.city,
            administrativeArea=self.administrativeArea,
            postalCode=self.postalCode,
            country=self.country
        )


class ScienceKeyword(models.Model):
    UUID = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    Category = models.CharField(max_length=128)
    Topic = models.CharField(max_length=128)
    Term = models.CharField(max_length=128)
    VariableLevel1 = models.CharField(max_length=128)
    VariableLevel2 = models.CharField(max_length=128)
    VariableLevel3 = models.CharField(max_length=128)
    DetailedVariable = models.CharField(max_length=128)

    def as_str(self):
        return ' | '.join(filter(
            lambda x: x,
            [self.Category, self.Topic, self.Term,
             self.VariableLevel1, self.VariableLevel2, self.VariableLevel3, self.DetailedVariable]))

    class Meta:
        ordering = ['Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable']
