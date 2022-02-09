import datetime
import os
import shutil
import stat
import urllib.parse
from tempfile import TemporaryFile, NamedTemporaryFile
from zipfile import ZipFile, ZipInfo

from django.conf import settings
from django.contrib import messages
from django.contrib.auth import logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.sites.shortcuts import get_current_site
from django.db.models import Q
from django.http import HttpResponse, HttpRequest
from django.middleware import csrf
from django.shortcuts import get_object_or_404, render
from django.shortcuts import redirect
from django.template import Context, Template
from django.urls import reverse
from django.utils.encoding import smart_text
from django_fsm import has_transition_perm
from elasticsearch_dsl import connections
from lxml import etree
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.exceptions import PermissionDenied
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.permissions import IsAuthenticated, IsAuthenticatedOrReadOnly
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response
from rest_framework.views import APIView

import requests

from metcalf.common import spec4, xmlutils5
from metcalf.common import xmlutils4
from metcalf.common.serializers import UserByEmailSerializer, UserSerializer
from metcalf.common.utils import to_json, get_exception_message
from metcalf.tern.backend.models import DraftMetadata, Document, DocumentAttachment, \
    AnzsrcKeyword, MetadataTemplate, TopicCategory, Person, Institution
from metcalf.tern.frontend.forms import DocumentAttachmentForm
from metcalf.tern.frontend.models import SiteContent
from metcalf.tern.frontend.permissions import is_document_editor, is_document_contributor
from metcalf.tern.frontend.serializers import DocumentInfoSerializer, AttachmentSerializer, \
    SiteContentSerializer, CreateDocumentSerializer


# def theme_keywords():
#     return ScienceKeyword.objects.all().exclude(Topic="").values_list(
#         'UUID', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3')


def anzsrc_keywords():
    return AnzsrcKeyword.objects.all().exclude(Topic="").values_list(
        'UUID', 'Topic', 'Term', 'VariableLevel1', 'VariableLevel2', 'VariableLevel3')


# def topic_categories():
#     return TopicCategory.objects.all().values_list(
#         'identifier', 'name')


def master_urls():
    return {
        "LandingPage": reverse("LandingPage"),
        "Dashboard": reverse("Dashboard"),
        "Create": reverse("Create"),
        "account_logout": reverse("Sign Out"),
        "STATIC_URL": settings.STATIC_URL,
    }


def messages_payload(request):
    return [{"level": message.level,
             "message": message.message,
             "extra_tags": message.tags}
            for message in messages.get_messages(request)]


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
            .filter(Q(owner=request.user) | Q(contributors=request.user))
            .exclude(status=Document.DISCARDED))
    payload = JSONRenderer().render({
        "context": {
            "urls": master_urls(),
            "URL_ROOT": settings.FORCE_SCRIPT_NAME or "",
            "site": site_content(get_current_site(request)),
            "user": UserSerializer(request.user).data,
            "documents": DocumentInfoSerializer(docs, many=True, context={'user': request.user}).data,
            "status": user_status_list(),
            "csrf": csrf.get_token(request),
        },
        "create_form": {
            "url": reverse("Create"),
            "data": {},
            "schema": {
                "type": "object",
                "properties": {
                    "title": {
                        "type": "string",
                        "label": "Title",
                        "rules": ["requiredField"]
                    },
                    "template": {
                        "type": "object",
                        "label": "Template",
                        "rules": ["requiredField"],
                        "properties": {
                            "id": {"type": "number"},
                            "name": {"type": "string"}
                        }
                    }
                }
            }
        },
        "page": {"name": request.resolver_match.url_name}})
    return render(request, "app.html", {"payload": smart_text(payload)})


@login_required
@api_view(['POST'])
def create(request):
    serializer = CreateDocumentSerializer(data=request.data)

    if not serializer.is_valid():
        return Response(data=serializer.errors, status=400)

    template = get_object_or_404(
        MetadataTemplate, site=get_current_site(request), archived=False, pk=request.data['template']['id'])

    doc = Document(title=request.data['title'],
                   owner=request.user,
                   template=template)
    doc.save()

    return Response({"message": "Created",
                     "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})


@login_required
@api_view(['GET'])
def extract_xml_data(request, template_id):
    template = get_object_or_404(
        MetadataTemplate, site=get_current_site(request), archived=False, pk=template_id)
    tree = etree.parse(template.file.path)
    spec = spec4.make_spec(mapper=template.mapper)
    data = xmlutils4.extract_xml_data(tree, spec)
    return Response({"data": data})


@login_required
@api_view(['GET'])
def extract_xml_data2(request, template_id):
    template = get_object_or_404(
        MetadataTemplate, site=get_current_site(request), archived=False, pk=template_id)
    tree = etree.parse(template.file.path)
    spec = spec4.make_spec(mapper=template.mapper)
    kwargs = {}
    if xmlutils4.has_namespaces(spec):
        kwargs['namespaces'] = xmlutils4.get_namespaces(spec)
    parsers = {
        'not_empty': xmlutils5.extract2_not_empty_parser,
        'text_string': xmlutils5.extract2_text_string_parser,
        'text_number': xmlutils5.extract2_text_number_parser
    }
    hit, data = xmlutils5.extract2(tree, spec, parsers, **kwargs)
    if hit:
        return Response({"hit": hit, "data": data})
    else:
        return Response({"hit": hit})


@login_required
@api_view(['GET'])
def analyse_metadata_template(request, template_id):
    template = get_object_or_404(
        MetadataTemplate, site=get_current_site(request), archived=False, pk=template_id)
    tree = etree.parse(template.file.path)
    payload = template.mapper.file.read().decode('utf-8')
    full_schema = spec4.analyse_schema(payload=payload)
    spec = spec4.compile_spec(payload=payload)
    data = xmlutils4.extract_xml_data(tree, spec)

    schema_with_analysis = xmlutils4.xpath_analysis(tree, full_schema)

    return Response({
        "data": data,
        "schema": schema_with_analysis
    })


@login_required
@api_view(['POST'])
def clone(request, uuid):
    orig_doc = get_object_or_404(Document, uuid=uuid)
    is_document_contributor(request, orig_doc)
    try:
        doc = Document.objects.clone(orig_doc, request.user)
        return Response({"message": "Cloned",
                         "document": DocumentInfoSerializer(doc, context={'user': request.user}).data})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
@api_view(['POST'])
def share(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    serializer = UserByEmailSerializer(data=request.data)
    if serializer.is_valid():
        email = serializer.validated_data['email']
        users = doc.contributors.filter(email=email)
        user = User.objects.get(email=email)
        if doc.owner == user:
            pass
        elif users.exists():
            pass
        else:
            doc.contributors.add(user)
        return Response({'status': 'Success'})
    else:
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@login_required
@api_view(['POST'])
def unshare(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_editor(request, doc)
    serializer = UserByEmailSerializer(data=request.data)
    if serializer.is_valid():
        email = serializer.validated_data['email']
        users = doc.contributors.filter(email=email)
        for user in users:
            doc.contributors.remove(user)
        return Response({'status': 'Success'})

    else:
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@login_required
def validation_results(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_contributor(request, doc)
    response_string = doc.validation_result
    # try to make it look pretty, but if not, just the raw text is fine
    try:
        response_string = etree.tostring(etree.fromstring(doc.validation_result.encode('UTF-8')), pretty_print=True)
    except:
        pass
    response = HttpResponse(response_string, content_type="application/xml")
    response['Content-Disposition'] = 'attachment; filename="{}-validation-results.xml"'.format(uuid)
    return response


# Error Pages
def server_error(request):
    response = render(request, "errors/500.html")
    response.status_code = 500
    return response


def bad_request(request, exception):
    response = render(request, "errors/400.html")
    response.status_code = 400
    return response


def create_export_xml_string(doc, uuid):
    data = to_json(doc.latest_draft.data)
    xml = etree.parse(doc.template.file.path)
    spec = spec4.make_spec(uuid=uuid, mapper=doc.template.mapper)
    xmlutils4.data_to_xml(data=data, xml_node=xml, spec=spec, nsmap=spec['namespaces'],
                          element_index=0, silent=True, fieldKey=None, doc_uuid=uuid)
    xmlutils5.export2(
        data=data,
        xml_node=xml,
        spec=spec,
        xml_kwargs={"namespaces": spec['namespaces']},
        handlers={
            "generateParameterKeywords": xmlutils5.export2_generateParameterKeywords_handler,
            "generateUnitKeywords": xmlutils5.export2_generateUnitKeywords_handler,
            "generateDatasourceDistributions": xmlutils5.export2_generateDatasourceDistributions_handler,
        })
    return etree.tostring(xml)


@login_required
def export(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_contributor(request, doc)
    response = HttpResponse(create_export_xml_string(doc, uuid), content_type="application/xml")
    if "download" in request.GET:
        response['Content-Disposition'] = 'attachment; filename="{}.xml"'.format(uuid)
    return response


def publish_to_geonetwork(doc, uuid):
    gn_root = settings.GEONETWORK_URLROOT
    gn_user = settings.GEONETWORK_USER
    gn_pass = settings.GEONETWORK_PASSWORD
    try:
        # export
        exported_doc = create_export_xml_string(doc, uuid)
        with requests.Session() as session:
            # Retrieve XSRF token:
            res = session.post(f"{gn_root}/srv/eng/info?type=me")
            token = res.cookies['XSRF-TOKEN']
            # publish
            res = session.post(f"{gn_root}/srv/api/records",
                               auth=(gn_user,gn_user),
                               headers={'X-XSRF-TOKEN': token,
                                        'Accept': 'application/json'},
                               data={'metadataType': 'METADATA',
                                     # Shared creates the UUID:
                                     'uuidProcessing': 'NOTHING'},
                               files={'file': ('metadata.xml', exported_doc)})
            # (update model)
    except Exception as e:
        # update model with error
        # raise?
        pass


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
    is_document_contributor(request, doc)
    data = to_json(doc.latest_draft.data)
    xml = etree.parse(doc.template.file.path)
    spec = spec4.make_spec(uuid=uuid, mapper=doc.template.mapper)
    xmlutils4.data_to_xml(data=data, xml_node=xml, spec=spec, nsmap=spec['namespaces'],
                          element_index=0, silent=True, fieldKey=None, doc_uuid=uuid)
    response = HttpResponse(content_type="application/x-mef")
    response['Content-Disposition'] = 'attachment; filename="{}.mef"'.format(uuid)
    info = etree.fromstring(MEF_TEMPLATE.encode('utf-8'))
    now = datetime.datetime.now().isoformat()
    info.xpath('/info/general/createDate')[0].text = now
    info.xpath('/info/general/changeDate')[0].text = now
    info.xpath('/info/general/uuid')[0].text = str(uuid)
    private = etree.SubElement(info, "private")
    # NOTE we can't write directly to `response` (i.e. ZipFile(response, 'w'))
    # because it doesn't support `seek`, required by ZipFile.write
    tmp = TemporaryFile()
    with ZipFile(tmp, 'w') as z:
        z.writestr('metadata.xml', etree.tostring(xml))
        pubdir = ZipInfo('public/')
        pubdir.file_size = 0
        pubdir.external_attr |= 0x10  # MS-DOS directory flag
        # set execute permission on directories
        pubdir.external_attr |= (stat.S_IRWXU & 0xFFFF) << 16
        z.writestr(pubdir, '')
        privdir = ZipInfo('private/')
        privdir.file_size = 0
        privdir.external_attr |= 0x10  # MS-DOS directory flag
        # set execute permission on directories
        privdir.external_attr |= (stat.S_IRWXU & 0xFFFF) << 16
        z.writestr(privdir, '')
        for attachment in doc.attachments.all():
            # need a tempfile to fetch files from object store to put into zip - Python Zipfile can only deal with proper files or full data in memory
            temp = NamedTemporaryFile()
            shutil.copyfileobj(attachment.file, temp)
            name = os.path.basename(attachment.file.name)
            z.write(temp.name, 'private/' + name)
            etree.SubElement(private, "file", name=name, changeDate=attachment.modified.isoformat())
        z.writestr('info.xml', etree.tostring(info))
        z.close()
        tmp.seek(0)
        response.write(tmp.read())
        tmp.close()
    return response


def home(request):
    site = get_current_site(request)
    sitecontent, _ = SiteContent.objects.get_or_create(site=get_current_site(request))
    context = {
        'sitecontent': sitecontent,
        'site': site
    }
    context['homepage_image_url'] = Template(site.sitecontent.homepage_image).render(Context(context)).strip()
    return render(request, "home.html", context)


# def personFromData(data):
#     uri = data['uri']
#     if uri:
#         familyName = data['familyName']
#         givenName = data['givenName']
#         if not familyName and not givenName:
#             data['individualName'] = ''
#         elif not familyName:
#             data['individualName'] = familyName
#         elif not givenName:
#             data['individualName'] = givenName
#         else:
#             data['individualName'] = '{0}, {1}'.format(familyName, givenName)
#         try:
#             matchingPerson = Person.objects.get(uri=uri)
#             if matchingPerson.isUserAdded:
#                 matchingPerson.orgUri = data['organisationIdentifier'] or ''
#                 matchingPerson.givenName = data['givenName'] or ''
#                 matchingPerson.familyName = data['familyName'] or ''
#                 matchingPerson.orcid = data['orcid'] or ''
#                 matchingPerson.prefLabel = data['individualName'] or ''
#                 matchingPerson.electronicMailAddress = data['electronicMailAddress'] or ''
#                 matchingPerson.save()
#                 return matchingPerson
#         except Person.DoesNotExist:
#             inst = Person.objects.create(uri=uri,
#                                          orgUri=data['organisationIdentifier'] or '',
#                                          givenName=data['givenName'] or '',
#                                          familyName=data['familyName'] or '',
#                                          orcid=data['orcid'] or '',
#                                          prefLabel=data['individualName'] or '',
#                                          electronicMailAddress=data['electronicMailAddress'] or '',
#                                          isUserAdded=True)
#             inst.save()
#             return inst
#     return None


# def institutionFromData(data):
#     orgUri = data['organisationIdentifier']
#     city = None
#     if data.get('address', None):
#         city = data['address'].get('city', None)
#     if '||' in orgUri:
#         orgUri = orgUri[:orgUri.index('||')]
#     if orgUri:
#         try:
#             if city:
#                 matchingOrg = Institution.objects.get(uri=orgUri, city=city)
#             else:
#                 matchingOrg = Institution.objects.get(uri=orgUri)
#             if matchingOrg.isUserAdded:
#                 matchingOrg.prefLabel = data['organisationName']
#                 matchingOrg.organisationName = data['organisationName']
#                 matchingOrg.administrativeArea = data['address']['administrativeArea']
#                 matchingOrg.city = data['address']['city']
#                 matchingOrg.postalCode = data['address']['postalCode']
#                 matchingOrg.country = data['address']['country']
#                 matchingOrg.deliveryPoint = data['address']['deliveryPoint']
#                 matchingOrg.deliveryPoint2 = data['address']['deliveryPoint2']
#                 matchingOrg.save()
#         except Institution.DoesNotExist:
#             inst = Institution.objects.create(uri=orgUri,
#                                               prefLabel=data['organisationName'],
#                                               altLabel=data['organisationName'],
#                                               organisationName=data['organisationName'],
#                                               administrativeArea=data['address']['administrativeArea'],
#                                               city=data['address']['city'],
#                                               postalCode=data['address']['postalCode'],
#                                               country=data['address']['country'],
#                                               deliveryPoint=data['address']['deliveryPoint'],
#                                               deliveryPoint2=data['address']['deliveryPoint2'],
#                                               isUserAdded=True)
#             inst.save()


@login_required
@api_view(['POST'])
def save(request, uuid, update_number):
    doc = get_object_or_404(Document, uuid=uuid)
    latest_draft = doc.draftmetadata_set.all()[0]
    if latest_draft.pk != update_number:
        return Response(
            {"message": "Stale save",
             "args": {
                 'posted': update_number,
                 'latest': latest_draft.pk}},
            status=400)
    is_document_contributor(request, doc)
    spec = spec4.make_spec(uuid=uuid, mapper=doc.template.mapper)
    try:
        data = request.data
        doc.title = data['identificationInfo']['title'] or "Untitled"
        if (doc.status == doc.SUBMITTED):
            doc.resubmit()
        doc.hasUserDefined = bool(xmlutils4.extract_user_defined(data))
        doc.save()

        draft = DraftMetadata.objects.create(document=doc, user=request.user, data=data)
        draft.noteForDataManager = data.get('noteForDataManager') or ''
        draft.agreedToTerms = data.get('agreedToTerms') or False
        draft.doiRequested = data.get('doiRequested') or False
        draft.save()

        # Remove any attachments which are no longer mentioned in the XML.
        xml_names = tuple(map(lambda x: os.path.basename(x['file']),
                              data.get('attachments', [])))
        # TODO: the logic to find files based an os.path.basename seems te be flawed.
        #       it works as long as the assumption that all files are stored are stored at the same path holds.
        #       otherwise, we will run into problems
        for attachment in doc.attachments.all():
            name = os.path.basename(attachment.file.url)
            if name not in xml_names:
                # TODO: sholud we delete the actual file as well?
                #       deleting the model does not remove files from storage backend
                # TODO: if we leave files around we may want to think about some cleanup process
                # attachement.file.delete()
                attachment.delete()

        tree = etree.parse(doc.template.file.path)

        return Response({"messages": messages_payload(request),
                         "form": {
                             "url": reverse("Save", kwargs={'uuid': doc.uuid, 'update_number': draft.pk}),
                             "schema": spec4.extract_fields(spec),
                             "data": data,
                             "document": DocumentInfoSerializer(doc, context={'user': request.user}).data}})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@login_required
@api_view(['GET'])
def user_defined(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    draft = doc.draftmetadata_set.all()[0]
    data = to_json(draft.data)

    duma = xmlutils4.extract_user_defined(data, acc=[])

    return Response({"user_added": duma,
                     "data": data,
                     "data_id": uuid})


@login_required
def edit(request, uuid):
    doc = get_object_or_404(Document, uuid=uuid)
    is_document_contributor(request, doc)
    spec = spec4.make_spec(uuid=uuid, mapper=doc.template.mapper)

    draft = doc.draftmetadata_set.all()[0]
    data = to_json(draft.data)
    tree = etree.parse(doc.template.file.path)

    raw_payload = {
        "context": {
            "csrf": csrf.get_token(request),
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
            "url": reverse("Save", kwargs={'uuid': doc.uuid, 'update_number': draft.pk}),
            "schema": spec4.extract_fields(spec),
            "data": data,
        },
        "upload_form": {
            "url": reverse("Upload", kwargs={'uuid': doc.uuid}),
            "fields": {
                'csrfmiddlewaretoken': {
                    'type': 'hidden',
                    'initial': csrf.get_token(request),
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
        "data": data,
        "attachments": AttachmentSerializer(doc.attachments.all(), many=True).data,
        # FIXME: tidy up (remove this, but probably also associated code)
        # "theme": {"keywordsTheme": {"table": theme_keywords()},
        #           "keywordsThemeAnzsrc": {"table": anzsrc_keywords()}},
        # "topicCategories": {"table": topic_categories()},
        # "institutions": [inst.to_dict() for inst in Institution.objects.all()],
        "page": {"name": request.resolver_match.url_name}
    }

    if doc.template.ui_template:
        raw_payload["ui_payload"] = doc.template.ui_template.file.open().read()

    payload = smart_text(JSONRenderer().render(raw_payload), encoding='utf-8')
    return render(request, "app.html", {"payload": payload})


class UploadView(APIView):
    permission_classes = (IsAuthenticated,)
    parser_classes = (MultiPartParser, FormParser,)
    renderer_classes = (JSONRenderer,)

    def post(self, request, uuid):
        doc = get_object_or_404(Document, uuid=uuid)
        is_document_contributor(request, doc)
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
    is_document_contributor(request, attachment.document)
    try:
        attachment.delete()
        return Response({"message": "Deleted"})
    except RuntimeError as e:
        return Response({"message": get_exception_message(e), "args": e.args}, status=400)


@api_view(['GET'])
@permission_classes([IsAuthenticatedOrReadOnly])
def download_attachement(request, path):
    attachment = get_object_or_404(DocumentAttachment, file=path)
    # TODO: this breaks for previously existing files ... it always creates a swift url
    return redirect(attachment.file.storage._path(attachment.file.name))


# TODO: Looks like a bad security practice.  Filter transition values?
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


def robots_view(request):
    context = {}
    return render(request, "robots.txt", context, content_type="text/plain")


def first_or_value(x):
    if isinstance(x, list):
        return x[0] if x else None
    else:
        return x


# NOTE: assumption is that UI doesn't need any complicated values, just a simple object
# TODO: exclude annotations we don’t ever need (e.g. type, is_hosted_by, broader, hierarchy, selectable...)
# TODO: need to check this suits all use cases or needs individual finessing
def massage_source(source):
    return {k: first_or_value(v) for k, v in source.items()}


def es_results(data):
    """
    Normalise data returned from ES endpoint.

    Returns a list of source documents as results
    """
    return {"results": [massage_source(hit['_source']) for hit in data['hits']['hits']]}


@api_view(["GET", "POST"])
def qudt_units(request) -> Response:
    """Search QUDT Units Index

    Search QUDT Units Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query". E.g. ?query=kilo
    - POST supports a post body object. E.g. {"query": "kilo"}.

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_QUDTUNITS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "multi_match": {
                    "query": urllib.parse.unquote(query),
                    "type": "phrase_prefix",
                    "fields": ["label", "ucumCode"]
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {"size": result_size}
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(["GET", "POST"])
def tern_parameters(request) -> Response:
    """Search TERN Parameters Index

    Search TERN Parameters Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query". E.g. ?query=cover
    - POST supports a post body object. E.g. {"query": "cover"}.

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.

    Top-level parameters of the Parameters Scheme are filtered out.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_TERNPARAMETERS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["label", "altLabel"]
                        }
                    },
                    "filter": {
                        "term": {"is_top_concept": False}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort on the empty query search.
            "query": {
                "bool": {
                    "filter": {
                        "term": {"is_top_concept": False}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_platforms(request) -> Response:
    """Search TERN Platforms Index

    Search TERN Platforms Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query". E.g. ?query=alos
    - POST supports a post body object. E.g. {"query": "alos"}

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.

    OWL classes are filtered out via the selectable value.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_TERNPLATFORMS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["label", "altLabel"]
                        }
                    },
                    "filter": {
                        "term": {"selectable": "true"}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort on the empty query search.
            "query": {
                "bool": {
                    "filter": {
                        "term": {"selectable": "true"}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_instruments(request) -> Response:
    """Search TERN Instruments Index

    Search TERN Instruments Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query" and "selected_platform.
      E.g. ?query=EOS+70D&selected_platform=https://w3id.org/tern/resources/e729eba7-215a-4626-9d13-feece79c41ad

    - POST supports a post body object.
      E.g. {"query": "EOS 70D", "selected_platform": "https://w3id.org/tern/resources/e729eba7-215a-4626-9d13-feece79c41ad"}

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.

    If "selected_platform" is supplied, the hits are filtered by platform.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_TERNINSTRUMENTS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
        selected_platform = request.GET.get("selected_platform", None)
    elif request.method == "POST":
        query = request.data.get("query")
        selected_platform = request.data.get("selected_platform", None)
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["label", "altLabel"]
                        }
                    }
                }
            }
        }
        if selected_platform:
            body["filter"] = [
                {
                    "bool": {
                        "should": [
                            {
                                "term": {"platform.keyword": selected_platform}
                            },
                            # Include instruments that have "*" as an element in their platform array.
                            # This is used to allow the user to query and select digital cameras regardless
                            # of which platform they have selected.
                            # The idea is to keep the empty query search to include the direct instruments of a
                            # selected platform (i.e. an explicit relationship) while this non-empty query
                            # search includes digital cameras for any selected platform.
                            {
                                "term": {"platform.keyword": "*"}
                            }
                        ]
                    }
                }
            ]
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort on the empty query search.
        }
        if selected_platform:
            body["query"] = {
                "bool": {
                    "filter": {
                        "term": {"platform.keyword": selected_platform}
                    }
                }
            }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_people(request) -> Response:
    """Search TERN People Index

    Search TERN People Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query". E.g. ?query=alos
    - POST supports a post body object. E.g. {"query": "alos"}

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.

    OWL classes are filtered out via the selectable value.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_TERNPEOPLE
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["name", "email"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"name.keyword": "asc"}],  # Sort by name
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_orgs(request) -> Response:
    """Search TERN Organisations Index

    Search TERN Organisations Elasticsearch index using GET or POST. Returns an Elasticsearch multi_match query result.
    - GET supports the query parameter "query". E.g. ?query=alos
    - POST supports a post body object. E.g. {"query": "alos"}

    If "query" is not supplied or is an empty string, the first n hits of the default /_search endpoint is returned,
    where n is the ELASTICSEARCH_RESULT_SIZE set in the configuration.

    OWL classes are filtered out via the selectable value.
    """
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_TERNORGS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["name", "full_address_line"]
                        }
                    },
                    "filter": {
                        "term": {"is_dissolved": "false"}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"name.keyword": "asc"}],  # Sort by name
            "query": {
                "bool": {
                    "filter": {
                        "term": {"is_dissolved": "false"}
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def geonetwork_entries(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_GEONETWORK
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["uuid", "label", "uri", "abstract"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def anzsrc_keywords(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_ANZSRC
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


# FIXME: no data / schema in the dumped index so far
@api_view(['GET', 'POST'])
def aus_plantnames(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_PLANTNAMES
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["uuid", "label", "uri", "abstract"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def gcmd_horizontal(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_GCMDHORIZONTAL
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label", "uri"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def gcmd_sciencekeywords(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_GCMDSCIENCE
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label", "uri"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def gcmd_temporal(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_GCMDTEMPORAL
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label", "uri"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def gcmd_vertical(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_GCMDVERTICAL
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label", "uri"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_faunalnames(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_FAUNALNAMES
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["breadcrumb", "label", "uri"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"label.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_verticalcrs(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_VERTICALCRS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["code", "codeSpace", "description", "MD_ReferenceSystemTypeCode"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"code.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)


@api_view(['GET', 'POST'])
def tern_horizontalcrs(request: HttpRequest) -> Response:
    es = connections.get_connection()
    index_alias = settings.ELASTICSEARCH_INDEX_HORIZONTALCRS
    result_size = settings.ELASTICSEARCH_RESULT_SIZE

    if request.method == "GET":
        query = request.GET.get("query")
    elif request.method == "POST":
        query = request.data.get("query")
    else:
        raise

    if query:
        body = {
            "size": result_size,
            "query": {
                "bool": {
                    "must": {
                        "multi_match": {
                            "query": urllib.parse.unquote(query),
                            "type": "phrase_prefix",
                            "fields": ["code", "codeSpace", "description", "MD_ReferenceSystemTypeCode"]
                        }
                    }
                }
            }
        }
        data = es.search(index=index_alias, body=body)
    else:
        body = {
            "size": result_size,
            "sort": [{"code.keyword": "asc"}],  # Sort by title
        }
        data = es.search(index=index_alias, body=body)

    return Response(es_results(data), status=200)
