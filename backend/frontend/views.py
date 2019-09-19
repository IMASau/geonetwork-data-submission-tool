# from frontend.router import rest_serialize
import requests
from django.conf import settings
from django.contrib import messages
from django.contrib.auth.decorators import login_required
from django.contrib.admin.views.decorators import staff_member_required
from django.contrib.auth import logout
from django.contrib.auth.models import User
from django.contrib.sites.shortcuts import get_current_site
from django.http import HttpResponse
from django.shortcuts import get_object_or_404, render_to_response, render
from django.template.context_processors import csrf
from django.urls import reverse
from django.shortcuts import redirect
from django.utils.encoding import smart_text
from django_fsm import has_transition_perm
from lxml import etree
import urllib.parse
from rest_framework import serializers
from rest_framework.decorators import api_view
from rest_framework.exceptions import PermissionDenied
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.permissions import IsAuthenticated
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response
from rest_framework.views import APIView
from tempfile import TemporaryFile
from zipfile import ZipFile, ZipInfo

from backend.models import DraftMetadata, Document, DocumentAttachment, ScienceKeyword, AnzsrcKeyword, MetadataTemplate, TopicCategory, Person
from backend.spec import *
from backend.utils import to_json, get_exception_message
from backend.xmlutils import extract_fields, data_to_xml
from frontend.forms import DocumentAttachmentForm
from frontend.models import SiteContent
from frontend.permissions import is_document_editor


def theme_keywords():
    return ScienceKeyword.objects.all().exclude(Topic="").values_list(
        'UUID', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3')


def anzsrc_keywords():
    return AnzsrcKeyword.objects.all().exclude(Topic="").values_list(
        'UUID', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3')


def topic_categories():
    return TopicCategory.objects.all().values_list(
        'identifier', 'name')


def master_urls():
    return {
        "LandingPage": reverse("LandingPage"),
        "Dashboard": reverse("Dashboard"),
        "Create": reverse("Create"),
        "STATIC_URL": settings.STATIC_URL,
    }


def messages_payload(request):
    return [{"level": message.level,
             "message": message.message,
             "extra_tags": message.tags}
            for message in messages.get_messages(request)]


class UserSerializer(serializers.ModelSerializer):
    groups = serializers.StringRelatedField(many=True)
    permissions = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name', 'groups', 'permissions', 'is_superuser', 'is_staff')

    def get_permissions(self, obj):
        permissions = set()
        for p in obj.user_permissions.all():
            permissions.add(p)
        for g in obj.groups.all():
            for p in g.permissions.all():
                permissions.add(p)
        # TODO: might need applabel.codename format
        return ["{0}.{1}".format(p.content_type.app_label, p.codename) for p in permissions]


class UserInfoSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name')


class DocumentInfoSerializer(serializers.ModelSerializer):
    owner = UserInfoSerializer()
    url = serializers.SerializerMethodField()
    clone_url = serializers.SerializerMethodField()
    transition_url = serializers.SerializerMethodField()
    export_url = serializers.SerializerMethodField()
    last_updated = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    transitions = serializers.SerializerMethodField()

    class Meta:
        model = Document
        fields = ('uuid', 'title', 'owner', 'last_updated',
                  'url', 'clone_url', 'transition_url', 'export_url',
                  'status', 'transitions')

    def get_url(self, doc):
        return reverse("Edit", kwargs={'uuid': doc.uuid})

    def get_transition_url(self, doc):
        return reverse("Transition", kwargs={'uuid': doc.uuid})

    def get_clone_url(self, doc):
        return reverse("Clone", kwargs={'uuid': doc.uuid})

    def get_export_url(self, doc):
        return reverse("Export", kwargs={'uuid': doc.uuid})

    def get_last_updated(self, doc):
        drafts = doc.draftmetadata_set
        if drafts.exists():
            return drafts.all()[0].time

    def get_status(self, doc):
        return doc.get_status_display()

    def get_transitions(self, doc):
        return [t.method.__name__
                for t in doc.get_available_user_status_transitions(self.context['user'])]


class AttachmentSerializer(serializers.ModelSerializer):
    delete_url = serializers.SerializerMethodField()
    name = serializers.SerializerMethodField()

    class Meta:
        model = DocumentAttachment
        fields = ('id', 'name', 'file', 'created', 'modified', 'delete_url')

    def get_name(self, inst):
        return inst.file.name

    def get_delete_url(self, inst):
        return reverse("DeleteAttachment", kwargs={'uuid': inst.document.uuid, 'id': inst.id})


class SiteContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = SiteContent
        fields = ('title', 'organisation_url', 'email', 'tag_line', 'guide_pdf', 'terms_pdf',
                  'portal_title', 'portal_url')


def site_content(site):
    (inst, created) = SiteContent.objects.get_or_create(site=site)
    return SiteContentSerializer(inst).data


def user_status_list():
    return [choice
            for choice in Document._meta.get_field('status').choices
            if choice[0] != Document.DISCARDED]


@login_required
def dashboard(request):
    docs = (Document.objects
            .filter(owner=request.user)
            .exclude(status=Document.DISCARDED))
    payload = JSONRenderer().render({
        "context": {
            "urls": master_urls(),
            "URL_ROOT": settings.FORCE_SCRIPT_NAME or "",
            "site": site_content(get_current_site(request)),
            "user": UserSerializer(request.user).data,
            "documents": DocumentInfoSerializer(docs, many=True, context={'user': request.user}).data,
            "status": user_status_list()
        },
        "create_form": {
            "url": reverse("Create"),
            "fields": {
                "title": {
                    "label": "Document title",
                    "initial": "Untitled",
                    "value": "",
                    "required": False
                },
                "template": {
                    "label": "Template",
                    "value": None,
                    "options": [[t.pk, t.__str__()]
                                for t in MetadataTemplate.objects.filter(site=get_current_site(request), archived=False)],
                    "required": True
                }
            }
        },
        "messages": messages_payload(request),
        "page": {"name": request.resolver_match.url_name}})
    return render(request, "app.html", {"payload": smart_text(payload)})


@login_required
@api_view(['POST'])
def create(request):
    template = get_object_or_404(
        MetadataTemplate, site=get_current_site(request), archived=False, pk=request.data['template'])
    try:

        doc = Document(title=request.data['title'],
                       owner=request.user,
                       template=template)
        doc.save()

        return Response({"message": "Created",
                         "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})
    except AssertionError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)
    except Exception as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
@api_view(['POST'])
def clone(request, uuid):
    orig_doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, orig_doc)
    try:
        doc = Document.objects.clone(orig_doc, request.user)
        return Response({"message": "Cloned",
                         "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
def export(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    data = to_json(doc.latest_draft.data)
    xml = etree.parse(doc.template.file.path)
    spec = make_spec(science_keyword=ScienceKeyword, uuid=uuid, mapper=doc.template.mapper)
    data_to_xml(data, xml, spec, spec['namespaces'])
    response = HttpResponse(etree.tostring(xml), content_type="application/xml")
    if "download" in request.GET:
        response['Content-Disposition'] = 'attachment; filename="{}.xml"'.format(uuid)
    return response


MEF_TEMPLATE = '''<?xml version="1.0" encoding="UTF-8"?>
<info version="1.1">
  <general>
    <createDate></createDate>
    <changeDate></changeDate>
    <schema>iso19139.mcp</schema>
    <isTemplate>false</isTemplate>
    <format>full</format>
    <uuid></uuid>
  </general>
  <categories />
  <privileges>
    <group name="all">
      <operation name="view" />
      <operation name="download" />
    </group>
  </privileges>
  <public />
</info>
'''


@login_required
def mef(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    data = to_json(doc.latest_draft.data)
    xml = etree.parse(doc.template.file.path)
    spec = make_spec(science_keyword=ScienceKeyword, uuid=uuid, mapper=doc.template.mapper)
    data_to_xml(data, xml, spec, spec['namespaces'])
    response = HttpResponse(content_type="application/x-mef")
    response['Content-Disposition'] = 'attachment; filename="{}.mef"'.format(uuid)
    info = etree.fromstring(MEF_TEMPLATE)
    now = datetime.datetime.now().isoformat()
    info.xpath('/info/general/createDate')[0].text = now
    info.xpath('/info/general/changeDate')[0].text = now
    info.xpath('/info/general/uuid')[0].text = uuid
    private = etree.SubElement(info, "private")
    # NOTE we can't write directly to `response` (i.e. ZipFile(response, 'w'))
    # because it doesn't support `seek`, required by ZipFile.write
    tmp = TemporaryFile()
    with ZipFile(tmp, 'w') as z:
        z.writestr('metadata.xml', etree.tostring(xml))
        z.writestr(ZipInfo('public/'), '')
        z.writestr(ZipInfo('private/'), '')
        for attachment in doc.attachments.all():
            name = os.path.basename(attachment.file.name)
            z.write(attachment.file.path, 'private/' + name)
            etree.SubElement(private, "file", name=name, changeDate=attachment.modified.isoformat())
        z.writestr('info.xml', etree.tostring(info))
        z.close()
        tmp.seek(0)
        response.write(tmp.read())
        tmp.close()
    return response

def home(request):
    sitecontent, _ = SiteContent.objects.get_or_create(site=get_current_site(request))
    return render_to_response("home.html", {'sitecontent': sitecontent})

def personFromData(data):
    uri = data['uri']
    if uri:
        try:
            matchingPerson = Person.objects.get(uri=uri)
            if matchingPerson.isUserAdded:
                matchingPerson.orgUri = data['organisationIdentifier']
                matchingPerson.givenName = data['givenName']
                matchingPerson.familyName = data['familyName']
                matchingPerson.orcid = data['orcid'] or ''
                matchingPerson.prefLabel = data['givenName'] + ' ' + data['familyName']
                matchingPerson.electronicMailAddress = data['electronicMailAddress']
                matchingPerson.save()
        except Person.DoesNotExist:
            inst = Person.objects.create(uri=uri,
                                         orgUri=data['organisationIdentifier'],
                                         givenName=data['givenName'],
                                         familyName=data['familyName'],
                                         orcid=data['orcid'] or '',
                                         prefLabel=data['givenName'] + ' ' + data['familyName'],
                                         electronicMailAddress=data['electronicMailAddress'],
                                         isUserAdded=True)
            inst.save()

@login_required
@api_view(['POST'])
def save(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    spec = make_spec(science_keyword=ScienceKeyword, uuid=uuid, mapper=doc.template.mapper)
    try:
        data = request.data
        doc.title = data['identificationInfo']['title'] or "Untitled"
        if (doc.status == doc.SUBMITTED):
            doc.resubmit()
        doc.save()

        #update the publication date
        data['identificationInfo']['datePublication'] = today()

        inst = DraftMetadata.objects.create(document=doc, user=request.user, data=data)
        inst.noteForDataManager = data['noteForDataManager'] or ''
        inst.agreedToTerms = data['agreedToTerms'] or False
        inst.doiRequested = data['doiRequested'] or False
        inst.save()

        # add any new people to the database
        pointOfContacts = data['identificationInfo']['pointOfContact']
        citedResponsibleParties = data['identificationInfo']['citedResponsibleParty']

        for pointOfContact in pointOfContacts:
            personFromData(pointOfContact)

        for citedResponsibleParty in citedResponsibleParties:
            personFromData(citedResponsibleParty)


        # Remove any attachments which are no longer mentioned in the XML.
        xml_names = map(lambda x: os.path.basename(x['file']), data['attachments'])
        for attachment in doc.attachments.all():
            name = os.path.basename(attachment.file.url)
            if name not in xml_names:
                attachment.delete()

        tree = etree.parse(doc.template.file.path)
        return Response({"messages": messages_payload(request),
                         "form": {
                             "url": reverse("Edit", kwargs={'uuid': doc.uuid}),
                             "fields": extract_fields(tree, spec),
                             "data": data,
                             "document": DocumentInfoSerializer(doc, context={'user': request.user}).data}})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
def edit(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    spec = make_spec(science_keyword=ScienceKeyword, uuid=uuid, mapper=doc.template.mapper)

    draft = doc.draftmetadata_set.all()[0]
    data = to_json(draft.data)
    tree = etree.parse(doc.template.file.path)

    raw_payload = {
        "context": {
            "site": site_content(get_current_site(request)),
            "urls": master_urls(),
            "URL_ROOT": settings.FORCE_SCRIPT_NAME or "",
            "uuid": doc.uuid,
            "user": UserSerializer(request.user).data,
            "title": data['identificationInfo']['title'],
            "document": DocumentInfoSerializer(doc, context={'user': request.user}).data,
            "status": user_status_list()
        },
        "form": {
            "url": reverse("Save", kwargs={'uuid': doc.uuid}),
            "fields": extract_fields(tree, spec),
            "data": data,
        },
        "upload_form": {
            "url": reverse("Upload", kwargs={'uuid': doc.uuid}),
            "fields": {
                'csrfmiddlewaretoken': {
                    'type': 'hidden',
                    'initial': str(csrf(request)['csrf_token'])
                },
                'document': {
                    'type': 'hidden',
                    'initial': uuid,
                },
                'name': {
                    'type': 'text',
                    'required': True
                },
                'file': {
                    'type': 'file',
                    'required': True
                }
            },
            "data": {},
        },
        "messages": messages_payload(request),
        "data": data,
        "attachments": AttachmentSerializer(doc.attachments.all(), many=True).data,
        "theme": {"keywordsTheme": {"table": theme_keywords()},
                  "keywordsThemeAnzsrc": {"table": anzsrc_keywords()}},
        "topicCategories": {"table": topic_categories()},
        # "institutions": [inst.to_dict() for inst in Institution.objects.all()],
        "page": {"name": request.resolver_match.url_name}
    }

    payload = smart_text(JSONRenderer().render(raw_payload), encoding='utf-8')
    return render(request, "app.html", {"payload": payload})


def theme(request):
    "Stand alone endpoint for looking at themes.  Not required for production UI."
    payload = JSONRenderer().render({
        "theme": theme_keywords(),
        "page": {"name": request.resolver_match.url_name}})
    return render(request, "app.html", {"payload": payload})


class UploadView(APIView):
    permission_classes = (IsAuthenticated,)
    parser_classes = (MultiPartParser, FormParser,)
    renderer_classes = (JSONRenderer,)

    def post(self, request, uuid):
        doc = get_object_or_404(Document, uuid=uuid)
        is_document_editor(request, doc)
        form = DocumentAttachmentForm(request.POST, request.FILES)
        if form.is_valid():
            inst = form.save()
            return Response(AttachmentSerializer(inst).data, status=201)
        else:
            return Response(form.errors, status=400)


@login_required
@api_view(['DELETE'])
def delete_attachment(request, uuid, id):
    attachment = get_object_or_404(DocumentAttachment, id=id, document__uuid=uuid)
    is_document_editor(request, attachment.document)
    try:
        attachment.delete()
        return Response({"message": "Deleted"})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
@api_view(['POST'])
def transition(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    try:
        transition = getattr(doc, request.data['transition'])
        if not has_transition_perm(transition, request.user):
            raise PermissionDenied
        transition()
        doc.save()
        return Response({"message": "Success",
                         "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)

def logout_view(request):
    logout(request)
    abs_uri = urllib.parse.quote(request.build_absolute_uri('/'))
    return redirect(settings.OIDC_LOGOUT_ENDPOINT + '?redirect_uri=' + abs_uri)

