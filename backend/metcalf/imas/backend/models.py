import copy
import json
import logging
import traceback
import uuid

import treebeard.ns_tree as ns_tree
from django.contrib.auth.models import User
from django.core.exceptions import ValidationError
from django.db import models
from django.utils import timezone
from django_fsm import transition, FSMField
from lxml import etree
from rest_framework.renderers import JSONRenderer

from metcalf.common.emails import *
from metcalf.common.models import AbstractDocumentAttachment, AbstractDataFeed, AbstractDocument, \
    AbstractMetadataTemplate, AbstractMetadataTemplateMapper, AbstractDraftMetadata, AbstractUserInterfaceTemplate
from metcalf.common import spec4, xmlutils4
from metcalf.common.utils import to_json, get_exception_message, get_user_name

User.add_to_class("__str__", get_user_name)

logger = logging.getLogger(__name__)


class UserInterfaceTemplate(AbstractUserInterfaceTemplate):
    pass


class MetadataTemplateMapper(AbstractMetadataTemplateMapper):

    def clean(self):
        try:
            spec = spec4.make_spec(science_keyword=ScienceKeyword, mapper=self)
        except Exception as e:
            traceback.print_exc()
            raise ValidationError({'file': get_exception_message(e)})


class MetadataTemplate(AbstractMetadataTemplate):

    def clean(self):
        try:
            tree = etree.fromstring(self.file.read())
            spec = spec4.make_spec(science_keyword=ScienceKeyword, mapper=self.mapper)
            spec4.extract_fields(tree)
            data = xmlutils4.extract_xml_data(tree, spec)
            # FIXME data_to_xml will validate presence of all nodes in the template, but only when data is fully mocked up
            data = json.loads(JSONRenderer().render(data).decode('utf-8'))
            xmlutils4.data_to_xml(data=data, xml_node=tree, spec=spec, nsmap=spec['namespaces'],
                                  element_index=0, silent=True, fieldKey=None, doc_uuid='cleanmetadatatemplatetest')

        except Exception as e:
            traceback.print_exc()
            raise ValidationError({'file': get_exception_message(e)})


class DataFeed(AbstractDataFeed):
    IDLE = 'Idle'
    SCHEDULED = 'Scheduled'
    ACTIVE = 'Active'

    STATUS_CHOICES = (
        (IDLE, IDLE),
        (SCHEDULED, SCHEDULED),
        (ACTIVE, ACTIVE),
    )

    state = FSMField(default=SCHEDULED, choices=STATUS_CHOICES)

    # FIXME abstract model has this in Meta. Does it need to be here or there?
    class Meta:
        permissions = (
            ("datafeed_schedule", "Can schedule datafeed refresh"),
            ("datafeed_unschedule", "Can cancel scheduled datafeed schedule"),
            ("datafeed_admin", "Can administer datafeed"),
        )

    @transition(field=state, source=[IDLE], target=SCHEDULED,
                permission='backend.datafeed_schedule')
    def schedule(self):
        pass

    @transition(field=state, source=[SCHEDULED], target=IDLE,
                permission='backend.datafeed_unschedule')
    def unschedule(self):
        pass

    @transition(field=state, source=[SCHEDULED], target=ACTIVE,
                permission='backend.datafeed_admin')
    def start(self):
        self.last_refresh = timezone.now()
        self.last_output = ""

    @transition(field=state, source=[ACTIVE], target=IDLE,
                permission='backend.datafeed_admin')
    def success(self, msg=""):
        self.last_output = msg
        self.last_success = timezone.now()
        self.last_duration = self.last_success - self.last_refresh

    @transition(field=state, source=[ACTIVE], target=IDLE,
                permission='backend.datafeed_admin')
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
        new_data['attachments'] = []

        DraftMetadata.objects.create(
            document=doc,
            user=user,
            data=new_data)

        return doc


class Document(AbstractDocument):
    objects = DocumentManager()

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

    status = FSMField(default=DRAFT, choices=STATUS_CHOICES)

    # FIXME is this needed
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
            spec = spec4.make_spec(science_keyword=ScienceKeyword, uuid=self.uuid, mapper=self.template.mapper)
            data = xmlutils4.extract_xml_data(tree, spec)
            # make sure there is no newline in self.title
            if self.title:
                self.title = self.title.replace('\n', ' ').strip()
            data = data or {}
            identificationInfo = data.get('identificationInfo', {})
            identificationInfo['title'] = self.title or identificationInfo.get('title')
            data['identificationInfo'] = identificationInfo
            data['fileIdentifier'] = self.pk

            DraftMetadata.objects.create(document=self,
                                         user=self.owner,
                                         data=data)
        else:
            return super(Document, self).save(*args, **kwargs)

    ########################################################
    # Workflow (state) Transitions
    @transition(field=status, source=[DRAFT, SUBMITTED],
                target=ARCHIVED)
    def archive(self):
        pass

    @transition(field=status, source=ARCHIVED, target=DRAFT)
    def restore(self):
        self.clear_note()

    @transition(field=status, source=SUBMITTED, target=DRAFT,
                permission='backend.workflow_reject')
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

    @transition(field=status, source=SUBMITTED, target=UPLOADED,
                permission='backend.workflow_upload')
    def upload(self):
        email_user_upload_alert(self)

    @transition(field=status, source=UPLOADED, target=DISCARDED,
                permission='backend.workflow_discard')
    def discard(self):
        pass

    @transition(field=status, source=DISCARDED, target=ARCHIVED,
                permission='backend.workflow_recover')
    def recover(self):
        pass

    @transition(field=status, source=ARCHIVED, target=DISCARDED)
    def delete_archived(self):
        pass

    @transition(field=status, source=UPLOADED, target=DRAFT,
                permission='backend.workflow_restart')
    def restart(self):
        self.clear_note()

    def add_creator(self, creators, person, nsmap):
        creator = etree.SubElement(creators, 'creator')
        creatorName = etree.SubElement(creator, 'creatorName')
        creatorName.text = '{last}, {first}'.format(last=person['familyName'], first=person['givenName'])

    ########################################################

    def clear_note(self):
        data = self.latest_draft.data
        data['noteForDataManager'] = ''
        inst = DraftMetadata(document=self,
                             user=self.latest_draft.user,
                             data=data,
                             noteForDataManager='')
        inst.save()


class DraftMetadata(AbstractDraftMetadata):
    # FIXME
    class Meta:
        verbose_name_plural = "Draft Metadata"
        ordering = ["-time"]


class DocumentAttachment(AbstractDocumentAttachment):
    objects = DocumentManager()


# TODO: Should this be a separate app?  Does workflow complicate this?
class Person(models.Model):
    id = models.AutoField(primary_key=True)
    uri = models.CharField(max_length=512, default="")
    orgUri = models.CharField(max_length=512, default="", blank=True)
    familyName = models.CharField(max_length=256, verbose_name="family name", blank=True)
    givenName = models.CharField(max_length=256, verbose_name="given name", blank=True)
    honorificPrefix = models.CharField(max_length=256, blank=True, verbose_name="honorific")
    orcid = models.CharField(max_length=50, verbose_name="ORCID ID", default="", blank=True)
    prefLabel = models.CharField(max_length=512, default="", blank=True)
    isUserAdded = models.BooleanField(default=False, verbose_name="User Added")
    electronicMailAddress = models.CharField(max_length=256, default="", verbose_name='email', blank=True)

    def __str__(self):
        return self.uri

    class Meta:
        ordering = ['familyName', 'givenName']


class Institution(models.Model):
    # TERN fields
    # http://linkeddata.tern.org.au/viewer/tern/id/http://linkeddata.tern.org.au/def/org

    id = models.AutoField(primary_key=True)
    uri = models.CharField(max_length=512, default="")
    prefLabel = models.CharField(max_length=512, default="")
    altLabel = models.CharField(max_length=512, default="")
    exactMatch = models.CharField(max_length=512, default="")
    isUserAdded = models.BooleanField(default=False, verbose_name="User Added")

    # EDMO fields
    # http://seadatanet.maris2.nl/v_edmo/browse_export.asp?order=&step=&count=0
    # Note: not a direct mapping, business logic applied in process.

    organisationName = models.CharField(max_length=256, verbose_name="organisation name")
    deliveryPoint = models.CharField(max_length=256, verbose_name="street address")
    deliveryPoint2 = models.CharField(max_length=256, verbose_name="street address 2")
    city = models.CharField(max_length=128)
    administrativeArea = models.CharField(max_length=100, verbose_name="state")
    postalCode = models.CharField(max_length=100, verbose_name="postcode")  # Seen one that's 30chars; be conservative!
    country = models.CharField(max_length=100)

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

    def __str__(self):
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
    uri = models.CharField(max_length=512, default="", blank=True, null=True)

    def as_str(self):
        return ' | '.join(filter(
            lambda x: x,
            [self.Category, self.Topic, self.Term,
             self.VariableLevel1, self.VariableLevel2, self.VariableLevel3, self.DetailedVariable, self.uri]))

    class Meta:
        ordering = ['Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable']


class AnzsrcKeyword(models.Model):
    UUID = models.CharField(max_length=256, primary_key=True, default='', editable=False, verbose_name="URL")
    Category = models.CharField(max_length=128)
    Topic = models.CharField(max_length=128)
    Term = models.CharField(max_length=128)
    VariableLevel1 = models.CharField(max_length=128)
    VariableLevel2 = models.CharField(max_length=128)
    VariableLevel3 = models.CharField(max_length=128)
    DetailedVariable = models.CharField(max_length=128)
    # the "UUID" column is already a URL here, but we want to keep the two science keyword tables the same
    uri = models.CharField(max_length=512, default="", blank=True, null=True)

    def as_str(self):
        return ' | '.join(filter(
            lambda x: x,
            [self.Category, self.Topic, self.Term,
             self.VariableLevel1, self.VariableLevel2, self.VariableLevel3, self.DetailedVariable, self.uri]))

    class Meta:
        ordering = ['Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable']


class RoleCode(models.Model):
    UUID = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    Identifier = models.CharField(max_length=128)
    Description = models.CharField(max_length=256)

    def as_str(self):
        return ' | '.join(filter(
            lambda x: x,
            [self.Identifier, self.Description]))

    class Meta:
        ordering = ['Identifier', 'Description']


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# https://vocabs.ands.org.au/aodn-discovery-parameter-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-discovery-parameter-vocabulary_version-1-1
class ParameterName(ns_tree.NS_Node):
    id = models.AutoField(primary_key=True)
    URI = models.CharField(max_length=128, db_column='URI')
    Name = models.CharField(max_length=128, db_column='Name')
    # Largest definition entry so far seen is 488 characters:
    Definition = models.CharField(max_length=4096, db_column='Definition')

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


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# https://vocabs.ands.org.au/aodn-units-of-measure-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-units-of-measure-vocabulary_version-1-0
class ParameterUnit(ns_tree.NS_Node):
    id = models.AutoField(primary_key=True)
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


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# https://vocabs.ands.org.au/aodn-instrument-vocabulary
# http://vocabs.ands.org.au/repository/api/sparql/aodn_aodn-instrument-vocabulary_version-1-0
class ParameterInstrument(ns_tree.NS_Node):
    id = models.AutoField(primary_key=True)
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
    id = models.AutoField(primary_key=True)
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


class SamplingFrequency(models.Model):
    uri = models.CharField(primary_key=True, max_length=512, default="")
    prefLabel = models.CharField(max_length=256)
    prefLabelSortText = models.CharField(max_length=256, default="")


class HorizontalResolution(models.Model):
    uri = models.CharField(primary_key=True, max_length=512, default="")
    prefLabel = models.CharField(max_length=256)
    prefLabelSortText = models.CharField(max_length=256, default="")


class TopicCategory(models.Model):
    identifier = models.CharField(primary_key=True, max_length=256)
    name = models.CharField(max_length=256)

    class Meta:
        ordering = ['identifier']
