import copy
import json
import treebeard.ns_tree as ns_tree
import uuid
from django.contrib.auth.models import User
from django.core.exceptions import ValidationError
from django.core.urlresolvers import reverse
from django.db import models
from django.utils import timezone
from django_fsm import FSMField, transition
from jsonfield import JSONField
from lxml import etree
from rest_framework.renderers import JSONRenderer

from backend.emails import *
from backend.spec_2_0 import make_spec
from backend.utils import to_json
from backend.xmlutils import extract_xml_data, data_to_xml, extract_fields


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


class DataFeed(models.Model):
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

    @transition(field=state, source=[IDLE], target=SCHEDULED, permission='backend.datafeed_schedule')
    def schedule(self):
        pass

    @transition(field=state, source=[SCHEDULED], target=IDLE, permission='backend.datafeed_unschedule')
    def unschedule(self):
        pass

    @transition(field=state, source=[SCHEDULED], target=ACTIVE, permission='backend.datafeed_admin')
    def start(self):
        self.last_refresh = timezone.now()
        self.last_output = ""

    @transition(field=state, source=[ACTIVE], target=IDLE, permission='backend.datafeed_admin')
    def success(self, msg=""):
        self.last_output = msg
        self.last_success = timezone.now()
        self.last_duration = self.last_success - self.last_refresh

    @transition(field=state, source=[ACTIVE], target=IDLE, permission='backend.datafeed_admin')
    def failure(self, msg=""):
        self.last_output = msg
        self.last_failure = timezone.now()
        self.last_duration = self.last_failure - self.last_refresh


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

    def save(self, *args, **kwargs):
    
        # Automatically add a draft metadata if none yet exist.
        if not self.pk or len(DraftMetadata.objects.filter(document=self).all()) < 1:
            super(Document, self).save(*args, **kwargs)
            
            tree = etree.parse(self.template.file.path)
            spec = make_spec(science_keyword=ScienceKeyword, uuid=self.uuid)
            data = extract_xml_data(tree, spec)
            data['identificationInfo']['title'] = self.title or data['identificationInfo']['title']
            data['fileIdentifier'] = self.pk
            DraftMetadata.objects.create(document=self,
                               user=self.owner,
                               data=JSONRenderer().render(data))
        else:
            return super(Document, self).save(*args, **kwargs)
        
    ########################################################
    # Workflow (state) Transitions
    @transition(field=status, source=[DRAFT, SUBMITTED], target=ARCHIVED)
    def archive(self):
        pass

    @transition(field=status, source=[ARCHIVED], target=DRAFT)
    def restore(self):
        self.clear_note()

    @transition(field=status, source=SUBMITTED, target=DRAFT, permission='backend.workflow_reject')
    def reject(self):
        self.clear_note()

    @transition(field=status, source=DRAFT, target=SUBMITTED)
    def submit(self):
        email_user_submit_confirmation(self)
        email_manager_submit_alert(self)

    @transition(field=status, source=SUBMITTED, target=SUBMITTED)
    def resubmit(self):
        self.clear_note()
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
        self.clear_note()

    ########################################################
    @property
    def latest_draft(self):
        return self.draftmetadata_set.all()[0]

    def clear_note(self):
        data = self.latest_draft.data
        data['noteForDataManager'] = ''
        inst = DraftMetadata(document=self,
                             user=self.latest_draft.user,
                             data=data,
                             noteForDataManager='')
        inst.save()

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
    noteForDataManager = models.TextField(default="")

    class Meta:
        verbose_name_plural = "Draft Metadata"
        ordering = ["-time"]


def no_spaces_in_filename(instance, filename):
    return filename.replace(" ", "_")


class DocumentAttachment(models.Model):
    document = models.ForeignKey("Document", related_name='attachments')
    name = models.CharField(max_length=256)
    file = models.FileField(upload_to=no_spaces_in_filename)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    class Meta:
        pass


class Institution(models.Model):

    # AODN fields
    # https://vocabs.ands.org.au/aodn-organisation-vocabulary
    # http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-organisation-vocabulary_version-1-2

    uri = models.CharField(max_length=512, default="")
    prefLabel = models.CharField(max_length=512, default="")
    altLabel = models.CharField(max_length=512, default="")
    exactMatch = models.CharField(max_length=512, default="")

    # EDMO fields
    # http://seadatanet.maris2.nl/v_edmo/browse_export.asp?order=&step=&count=0
    # Note: not a direct mapping, business logic applied in process.

    organisationName = models.CharField(max_length=256, verbose_name="organisation name")
    deliveryPoint = models.CharField(max_length=256, verbose_name="street address")
    deliveryPoint2 = models.CharField(max_length=256, verbose_name="street address 2")
    city = models.CharField(max_length=128)
    administrativeArea = models.CharField(max_length=64, verbose_name="state")
    postalCode = models.CharField(max_length=64, verbose_name="postcode")  # Seen one that's 30chars; be conservative!
    country = models.CharField(max_length=64)

    def to_dict(self):
        return dict(
            uri=self.uri,
            prefLabel=self.prefLabel,
            altLabel=self.altLabel,
            exactMatch=self.exactMatch,
            organisationName=self.organisationName,
            deliveryPoint=self.deliveryPoint,
            deliveryPoint2=self.deliveryPoint2,
            city=self.city,
            administrativeArea=self.administrativeArea,
            postalCode=self.postalCode,
            country=self.country
        )

    def __unicode__(self):
        return self.uri


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


class RoleCode(models.Model):
    UUID = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    Identifier = models.CharField(max_length=128)
    Description = models.CharField(max_length=128)

    def as_str(self):
        return ' | '.join(filter(
            lambda x: x,
            [self.Identifier, self.Description]))

    class Meta:
        ordering = ['Identifier', 'Description']


# https://vocabs.ands.org.au/aodn-discovery-parameter-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-discovery-parameter-vocabulary_version-1-1
class ParameterName(ns_tree.NS_Node):
    URI = models.CharField(max_length=128, db_column='URI')
    Name = models.CharField(max_length=128, db_column='Name')
    # Largest definition entry so far seen is 488 characters:
    Definition = models.CharField(max_length=1024, db_column='Definition')

    is_selectable = models.BooleanField()
    Version = models.CharField(max_length=16, db_column="Version")  # eg "version-1-0"

    class Meta:
        ordering = ['Name']

    @property
    def term(self):
        return self.Name

    @property
    def vocabularyTermURL(self):
        return self.URI

    @property
    def vocabularyVersion(self):
        return self.Version

    @property
    def termDefinition(self):
        return self.Definition


# https://vocabs.ands.org.au/aodn-units-of-measure-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-units-of-measure-vocabulary_version-1-0
class ParameterUnit(ns_tree.NS_Node):
    URI = models.CharField(max_length=128, db_column='URI')
    Name = models.CharField(max_length=128, db_column='Name')
    Definition = models.CharField(max_length=256, db_column='Definition')

    is_selectable = models.BooleanField()
    Version = models.CharField(max_length=16, db_column="Version")  # eg "version-1-0"

    class Meta:
        ordering = ['Name']

    @property
    def term(self):
        return self.Name

    @property
    def vocabularyTermURL(self):
        return self.URI

    @property
    def vocabularyVersion(self):
        return self.Version

    @property
    def termDefinition(self):
        return self.Definition


# https://vocabs.ands.org.au/aodn-instrument-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-instrument-vocabulary_version-1-0
class ParameterInstrument(ns_tree.NS_Node):
    URI = models.CharField(max_length=128, db_column='URI')
    Name = models.CharField(max_length=128, db_column='Name')
    # Largest definition entry so far seen is 1252 characters:
    Definition = models.CharField(max_length=2500, db_column='Definition')

    is_selectable = models.BooleanField()
    Version = models.CharField(max_length=16, db_column="Version")  # eg "version-1-0"

    class Meta:
        ordering = ['Name']

    @property
    def term(self):
        return self.Name

    @property
    def vocabularyTermURL(self):
        return self.URI

    @property
    def vocabularyVersion(self):
        return self.Version

    @property
    def termDefinition(self):
        return self.Definition


# https://vocabs.ands.org.au/aodn-platform-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-platform-vocabulary_version-1-2
class ParameterPlatform(ns_tree.NS_Node):
    URI = models.CharField(max_length=128, db_column='URI')
    Name = models.CharField(max_length=128, db_column='Name')
    # Largest definition entry so far seen is 2154 characters:
    Definition = models.CharField(max_length=5000, db_column='Definition')

    is_selectable = models.BooleanField()
    Version = models.CharField(max_length=16, db_column="Version")  # eg "version-1-0"

    class Meta:
        ordering = ['Name']

    @property
    def term(self):
        return self.Name

    @property
    def vocabularyTermURL(self):
        return self.URI

    @property
    def vocabularyVersion(self):
        return self.Version

    @property
    def termDefinition(self):
        return self.Definition
