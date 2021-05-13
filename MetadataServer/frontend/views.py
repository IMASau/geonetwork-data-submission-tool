# from frontend.router import rest_serialize
from django.contrib import messages
from django.contrib.auth.models import User
from django.conf import settings
from django.urls import reverse
from django_fsm import has_transition_perm
from rest_framework import serializers
from rest_framework.decorators import api_view
from rest_framework.exceptions import PermissionDenied
from rest_framework.response import Response
from rest_framework.renderers import JSONRenderer
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView
from lxml import etree
from django.shortcuts import get_object_or_404, render_to_response
from django.contrib.auth.decorators import login_required
from django.http import HttpResponse
from django.template.context_processors import csrf

from backend.models import Institution, DraftMetadata, Document, DocumentAttachment, ScienceKeyword, MetadataTemplate
from backend.utils import to_json
from frontend.forms import DocumentAttachmentForm
from frontend.models import SiteContent
from frontend.permissions import is_document_editor
from backend.xmlutils import extract_xml_data, extract_fields, data_to_xml
from backend.spec_2_0 import *

spec = make_spec(science_keyword=ScienceKeyword)


def theme_keywords():
    return ScienceKeyword.objects.all().exclude(Topic="").values_list(
        'UUID', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3')


def master_urls():
    return {
        "LandingPage": reverse("LandingPage"),
        "Dashboard": reverse("Dashboard"),
        "Create": reverse("Create"),
        "account_signup": reverse("account_signup"),
        "account_login": reverse("account_login"),
        "account_logout": reverse("account_logout"),
        "account_profile": reverse("account_profile"),
        "account_change_password": reverse("account_change_password"),
        "account_email": reverse("account_email"),
        "account_reset_password": reverse("account_reset_password"),
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
    last_updated = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    transitions = serializers.SerializerMethodField()

    class Meta:
        model = Document
        fields = ('uuid', 'title', 'owner', 'last_updated',
                  'url', 'clone_url', 'transition_url',
                  'status', 'transitions')

    def get_url(self, doc):
        return reverse("Edit", kwargs={'uuid': doc.uuid})

    def get_transition_url(self, doc):
        return reverse("Transition", kwargs={'uuid': doc.uuid})

    def get_clone_url(self, doc):
        return reverse("Clone", kwargs={'uuid': doc.uuid})

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

    class Meta:
        model = DocumentAttachment
        fields = ('id', 'name', 'file', 'created', 'modified', 'delete_url')

    def get_delete_url(self, inst):
        return reverse("DeleteAttachment", kwargs={'uuid': inst.document.uuid, 'id': inst.id})


class SiteContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = SiteContent
        fields = ('title', 'organisation_url', 'email', 'tag_line', 'guide_pdf',
                  'portal_title', 'portal_url')


def site_content(site):
    (inst, created) = SiteContent.objects.get_or_create(site=site)
    return SiteContentSerializer(inst).data


def user_status_list():
    return [choice
            for choice in Document._meta.get_field('status').choices
            if choice[0] != Document.DISCARDED]


@login_required
@api_view()
def dashboard(request):
    docs = (Document.objects
            .filter(owner=request.user)
            .exclude(status=Document.DISCARDED))
    return Response({
        "context": {
            "urls": master_urls(),
            "site": site_content(request.site),
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
                    "required": True
                },
                "template": {
                    "label": "Template",
                    "value": None,
                    "options": [[t.pk, t.__str__()]
                                for t in MetadataTemplate.objects.filter(site=request.site, archived=False)],
                    "required": True
                }
            }
        },
        "messages": messages_payload(request),
        "page": {"name": request.resolver_match.url_name}})


@login_required
@api_view(['POST'])
def create(request):
    template = get_object_or_404(
        MetadataTemplate, site=request.site, archived=False, pk=request.data['template'])
    try:
        tree = etree.parse(template.file.path)
        doc = Document.objects.create(title=request.data['title'],
                                      owner=request.user,
                                      template=template)
        data = extract_xml_data(tree, spec)
        data['identificationInfo']['title'] = request.data['title']
        data['fileIdentifier'] = doc.pk
        DraftMetadata.objects.create(document=doc,
                                     user=request.user,
                                     data=JSONRenderer().render(data))
        return Response({"message": "Created",
                         "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})
    except AssertionError as e:
        return Response({"message": e.message[0], "args": e.args}, status=400)
    except Exception as e:
        return Response({"message": e.message, "args": e.args}, status=400)


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
        return Response({"message": e.message, "args": e.args}, status=400)


@login_required
def export(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    data = to_json(doc.draftmetadata_set.all()[0].data)
    xml = etree.parse(doc.template.file.path)
    data_to_xml(data, xml, spec, spec['namespaces'])
    return HttpResponse(etree.tostring(xml), content_type="application/xml")


def home(request):
    sitecontent, _ = SiteContent.objects.get_or_create(site=request.site)
    return render_to_response("home.html", {'sitecontent': sitecontent})


@login_required
@api_view(['GET', 'POST'])
def edit(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)

    if request.method == 'POST':
        doc.title = request.data['identificationInfo']['title'] or "Untitled"
        if (doc.status == doc.SUBMITTED):
            doc.resubmit()
        doc.save()
        inst = DraftMetadata.objects.create(document=doc, user=request.user, data=request.data)
        tree = etree.parse(doc.template.file.path)
        return Response({"messages": messages_payload(request),
                         "form": {
                             "url": reverse("Edit", kwargs={'uuid': doc.uuid}),
                             "fields": extract_fields(tree, spec),
                             "data": to_json(inst.data),
                             "document": DocumentInfoSerializer(doc, context={'user': request.user}).data}})

    draft = doc.draftmetadata_set.all()[0]
    data = to_json(draft.data)
    tree = etree.parse(doc.template.file.path)

    return Response({
        "context": {
            "site": site_content(request.site),
            "urls": master_urls(),
            "uuid": doc.uuid,
            "user": UserSerializer(request.user).data,
            "title": data['identificationInfo']['title'],
            "document": DocumentInfoSerializer(doc, context={'user': request.user}).data,
            "status": user_status_list()
        },
        "form": {
            "url": reverse("Edit", kwargs={'uuid': doc.uuid}),
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
        "theme": {"table": theme_keywords()},
        "institutions": [inst.to_dict() for inst in Institution.objects.all()],
        "page": {"name": request.resolver_match.url_name}})


@api_view()
def theme(request):
    "Stand alone endpoint for looking at themes.  Not required for production UI."
    return Response({
        "theme": theme_keywords(),
        "page": {"name": request.resolver_match.url_name}})


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
        return Response({"message": e.message, "args": e.args}, status=400)


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
        return Response({"message": e.message, "args": e.args}, status=400)
