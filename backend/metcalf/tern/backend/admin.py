import copy
import urllib
import urllib.parse

import requests
from django.contrib import admin
from django.contrib import messages
from django.contrib.admin import widgets
from django.db import models as django_models
from django.http import HttpResponseRedirect
from django.urls import reverse
from django.utils.html import format_html, format_html_join
from fsm_admin.mixins import FSMTransitionMixin
from lxml import etree

from metcalf.tern.backend import models
from metcalf.tern.frontend.models import SiteContent


class InstitutionAdmin(admin.ModelAdmin):
    list_display = ['prefLabel', 'organisationName', 'isUserAdded', 'deliveryPoint', 'city', 'administrativeArea',
                    'postalCode',
                    'country']
    search_fields = ['prefLabel', 'altLabel', 'uri',
                     'organisationName', 'deliveryPoint', 'deliveryPoint2', 'city', 'administrativeArea', 'postalCode',
                     'country']
    list_filter = ['country']
    readonly_fields = ['uri',
                       'prefLabel',
                       'altLabel',
                       'exactMatch',
                       'organisationName',
                       'deliveryPoint',
                       'deliveryPoint2',
                       'city',
                       'administrativeArea',
                       'postalCode',
                       'country']


class PersonAdmin(admin.ModelAdmin):
    list_display = ['familyName', 'givenName', 'honorificPrefix', 'prefLabel', 'electronicMailAddress', 'orcid',
                    'organisation', 'isUserAdded']
    search_fields = ['familyName', 'givenName', 'orcid']

    def organisation(self, obj):
        org = models.Institution.objects.filter(uri=obj.orgUri).first()
        if org == None:
            return format_html('<a href="{0}" target="_blank">{0}</a>', obj.orgUri)
        else:
            return org.organisationName


class SamplingFrequencyAdmin(admin.ModelAdmin):
    list_display = ['prefLabel']
    search_fields = ['prefLabel']


class HorizontalResolutionAdmin(admin.ModelAdmin):
    list_display = ['prefLabel']
    search_fields = ['prefLabel']


class TopicCategoryAdmin(admin.ModelAdmin):
    list_display = ['identifier']
    search_fields = ['identifier']


class DraftMetadataAdmin(admin.ModelAdmin):
    list_display = ['time', 'user', 'document']
    list_filter = ['time', 'user']
    search_fields = ['data', 'document__pk']


class MetadataTemplateAdmin(admin.ModelAdmin):
    list_display = ['name', 'file', 'site', 'notes']
    list_filter = ['archived', 'site', 'created', 'modified']
    ordering = ['modified']
    readonly_fields = ['created', 'modified']


class UserInterfaceTemplateAdmin(admin.ModelAdmin):
    list_display = ['name', 'file', 'site', 'notes']
    list_filter = ['archived', 'site', 'created', 'modified']
    ordering = ['modified']
    readonly_fields = ['created', 'modified']


class MetadataTemplateMapperAdmin(admin.ModelAdmin):
    list_display = ['name', 'file', 'site', 'notes']
    list_filter = ['archived', 'site', 'created', 'modified']
    ordering = ['modified']
    readonly_fields = ['created', 'modified']


class DocumentAttachmentInline(admin.TabularInline):
    model = models.DocumentAttachment


def bulk_scheduled(modeladmin, request, queryset):
    for datafeed in queryset.filter(state=models.DataFeed.IDLE):
        datafeed.schedule()
        datafeed.save()


bulk_scheduled.short_description = "Schedule refresh"


def bulk_unscheduled(modeladmin, request, queryset):
    for datafeed in queryset.filter(state=models.DataFeed.SCHEDULED):
        datafeed.unschedule()
        datafeed.save()


bulk_unscheduled.short_description = "Unschedule refresh"


class DataFeedAdmin(FSMTransitionMixin, admin.ModelAdmin):
    list_display = ["name", "state", "last_refresh", "last_success", "feed_quality"]
    readonly_fields = ["state",
                       "last_refresh",
                       "last_success",
                       "last_failure",
                       "last_duration",
                       "last_output"]
    actions = [bulk_scheduled, bulk_unscheduled]

    def feed_quality(self, obj):
        q = obj.feed_quality()
        if q == "Good":
            return format_html("<b>{0}</b> - {1}", "Good", "Most recent refresh succeeded")
        elif q == "Stale":
            return format_html("<b>{0}</b> - {1}", "Stale", "We have data but the most recent refresh failed")
        elif q == "Bad":
            return format_html("<b>{0}</b> - {1}", "Bad", "We have not successfully refreshed data")
        elif q == "Uninitialised":
            return format_html("<b>{0}</b> - {1}", "No data", "No refresh has been attempted")
        else:
            return format_html("<b>{0}</b>", q)

    feed_quality.short_description = "Feed quality"


def add_creator(creators, person, nsmap):
    creator = etree.SubElement(creators, 'creator')
    creatorName = etree.SubElement(creator, 'creatorName')
    creatorName.text = '{last}, {first}'.format(last=person['familyName'], first=person['givenName'])


class DocumentAdmin(FSMTransitionMixin, admin.ModelAdmin):
    list_display = ['admin_name', 'owner_name', 'status', 'validity', 'date_last_validated', 'publish_status',
                    'date_published', 'hasUserDefined', 'action_links']
    list_filter = ['status', 'template', 'hasUserDefined']
    search_fields = ['title', 'owner__username', 'owner__email', 'uuid']
    fsm_field = ['status', ]
    readonly_fields = ['status', 'action_links', 'submission_note', 'doi_links', 'validity', 'date_last_validated', 'hasUserDefined', 'publish_status', 'date_published', 'publish_result',]
    inlines = [DocumentAttachmentInline]
    autocomplete_fields=['contributors']
    fieldsets = [
        (None, {'fields': ('title', 'template', 'owner', 'contributors', 'categories', 'status', 'hasUserDefined', 'submission_note', 'doi')}),
        ('Validation', {'fields': ('validity', 'date_last_validated')}),
        ('Export', {'fields': ('action_links',)}),
        ('DOI Minting', {'fields': ('doi_links',)}),
    ]

    # a quick hack to make admin interface a bit nicer to use for title field
    formfield_overrides = {
        django_models.TextField: {'widget': widgets.AdminTextInputWidget(attrs={'style': "width:60em;"})}
    }

    # Override fieldsets to customise publication-related display
    def get_fieldsets(self, request, obj=None):
        # Initialise with the fieldsets declared above:
        fieldsets = super().get_fieldsets(request, obj)
        # Trap for beginners: if super.get_fieldsets returns the
        # admin-instance variable we will potentially append multiple
        # copies (ignoring the actual status of the object in
        # question)
        fieldsets = copy.deepcopy(fieldsets)
        if obj and obj.status in (models.Document.SUBMITTED, models.Document.UPLOADED):
            gn_fieldset = ('Publishing to Catalogue', {'fields': ('date_published', 'publish_result',)})
            fieldsets.append(gn_fieldset)
        return fieldsets

    def validity(self, obj):
        if obj.validation_status in ['Valid', 'Invalid']:
            htmlString = "<a href='{0}' target='_blank'>{1}</a>"
            replacements = [reverse('Validation', kwargs={'uuid': obj.uuid}), obj.validation_status]
            return format_html(htmlString, *replacements)
        else:
            htmlString = "<span>{0}</span>"
            replacements = [obj.validation_status]
            return format_html(htmlString, *replacements)

    def submission_note(self, obj):
        if obj.latest_draft:
            return obj.latest_draft.noteForDataManager

    def action_links(self, obj):
        htmlString = ("<a href='{0}' target='_blank'>Edit</a> | "
                      "<a href='{1}?download' target='_blank'>Export XML</a> | "
                      "<a href='{2}' target='_blank'>Export MEF</a>")
        replacements = [reverse('Edit', kwargs={'uuid': obj.uuid}),
                        reverse('Export', kwargs={'uuid': obj.uuid}),
                        reverse('MEF', kwargs={'uuid': obj.uuid})]
        return format_html(htmlString, *replacements)

    def owner_name(self, obj):
        if obj.owner.email:
            return obj.owner.email
        return obj.owner.username

    def doi_links(self, obj):
        try:
            wantsDoi = obj.latest_draft.doiRequested
            if not wantsDoi:
                return "User has not requested a DOI to be minted."
            # can mint if they asked for one and don't have one yet
            if bool(obj.doi):
                return "This document already has a DOI."

            if obj.status != models.Document.SUBMITTED:
                return "Cannot mint a DOI until the document is lodged."
        except:
            return "Cannot mint a DOI for this document"
        htmlString = "<input type='submit' value='Mint DOI' name='_mintdoi'/>"
        return format_html(htmlString)

    def mintdoi(self, request, obj):
        doc = obj
        doi_template = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <resource xsi:schemaLocation="http://datacite.org/schema/kernel-4
    http://schema.datacite.org/meta/kernel-4.2/metadata.xsd"
    xmlns="http://datacite.org/schema/kernel-4"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <identifier identifierType="DOI"/>
        <creators></creators>
        <titles><title></title></titles>
        <publisher>Terrestrial Ecosystem Research Network (TERN)</publisher>
        <publicationYear></publicationYear>
        <resourceType resourceTypeGeneral="Dataset">dataset</resourceType></resource>'''
        ns = {'n': 'http://datacite.org/schema/kernel-4'}
        request_xml = etree.fromstring(doi_template.encode('utf-8'))
        data = doc.latest_draft.data

        # all the people involved
        pocs = data['identificationInfo']['pointOfContact']
        crps = data['identificationInfo']['citedResponsibleParty']

        # add title
        request_xml.find('n:titles/n:title', ns).text = data['identificationInfo']['title']

        publicationYear = request_xml.find('n:publicationYear', ns)
        # might be nicer to convert to a date and explicitly pull the year maybe
        publicationYear.text = data['identificationInfo']['datePublication'][:4]

        author_uuids = []
        coauthor_uuids = []

        # <creator><creatorName></creatorName></creator>
        creators = request_xml.find('n:creators', ns)
        # we want to add the authors first, easier to just loop through twice
        # check the uri to make sure we don't add a person twice
        for person in pocs:
            if person['role'] == 'author' and person['uri'] not in author_uuids:
                add_creator(creators, person, ns)
                author_uuids.append(person['uri'])

        for person in crps:
            if person['role'] == 'author' and person['uri'] not in author_uuids:
                add_creator(creators, person, ns)
                author_uuids.append(person['uri'])

        for person in pocs:
            if person['role'] == 'coAuthor' and person['uri'] not in coauthor_uuids:
                add_creator(creators, person, ns)
                coauthor_uuids.append(person['uri'])

        for person in crps:
            if person['role'] == 'coAuthor' and person['uri'] not in coauthor_uuids:
                add_creator(creators, person, ns)
                coauthor_uuids.append(person['uri'])

        response = None
        try:
            if doc.latest_draft.doiRequested:
                # they have asked for a DOI to be minted, and it hasn't been done yet
                if not data.get('doi', None):
                    sitecontent = SiteContent.objects.all()[0]
                    baseUri = sitecontent.doi_uri
                    doiUri = urllib.parse.quote(
                        'https://geonetwork.tern.org.au/geonetwork/srv/eng/catalog.search#/metadata/{uuid}'.format(
                            uuid=data['fileIdentifier']))
                    requestUri = '{base}&url={doiUri}'.format(base=baseUri, doiUri=doiUri)
                    body = {'xml': etree.tostring(request_xml)}
                    response = requests.post(requestUri, data=body, verify=True)
        except Exception as e:
            messages.add_message(request,
                                 messages.ERROR,
                                 'The following error occurred while trying to mint a DOI "{e}". Please try again later.'.format(
                                     e=e))
            return False
        if response:
            if response.status_code == 200:
                response_xml = etree.fromstring(response.content)
                outcome = response_xml.attrib['type']
                if outcome == 'success':
                    message = response_xml.find('message').text
                    doi = response_xml.find('doi').text
                    doc.doi = doi
                    doc.save()
                else:
                    verboseMessage = response_xml.find('verbosemessage').text
                    messages.add_message(request,
                                         messages.ERROR,
                                         'The following error occurred while trying to mint a DOI "{verboseMessage}". Please try again later.'.format(
                                             verboseMessage=verboseMessage))
                    return False
            else:
                messages.add_message(request, messages.ERROR,
                                     'There was an error connecting to the DOI minting service (status code {status}). Please try again later.'.format(
                                         status=response.status_code))
                return False
        else:
            messages.add_message(request, messages.ERROR,
                                 'There was an error connecting to the DOI minting service. Please try again later.')
            return False
        messages.add_message(request, messages.SUCCESS, 'DOI successfully minted.')

    def response_change(self, request, obj):
        if "_mintdoi" in request.POST:
            try:
                success = self.mintdoi(request, obj)
            except Exception as e:
                messages.add_message(request, messages.ERROR,
                                     'There was an unexpected error while attempting to mint the DOI. Please try again later.')
            return HttpResponseRedirect(".")
        return super().response_change(request, obj)

    def admin_name(self, obj):
        return "{1} ({0})".format(str(obj.uuid)[:8], obj.short_title())

    action_links.short_description = "Actions"
    doi_links.short_description = "DOI Minting"
    owner_name.admin_order_field = 'owner__email'
    admin_name.admin_order_field = 'title'


class DocumentAttachmentAdmin(admin.ModelAdmin):
    list_display = ['document', 'file']


class DocumentCategoryAdmin(admin.ModelAdmin):
    list_display = ['id', 'name', 'label']
    readonly_fields = ['name', 'label']

class ScienceKeywordAdmin(admin.ModelAdmin):
    list_display = ['UUID', 'Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable', 'uri']
    list_filter = ['Category', 'Topic']
    search_fields = ['UUID', 'Category', 'Topic', 'Term',
                     'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                     'DetailedVariable']


class AnzsrcKeywordAdmin(admin.ModelAdmin):
    list_display = ['UUID', 'Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable', 'uri']
    list_filter = ['Category', 'Topic']
    search_fields = ['UUID', 'Category', 'Topic', 'Term',
                     'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                     'DetailedVariable']


class CommonVocabNodeAdmin(admin.ModelAdmin):
    list_display = ['Name', 'URI', 'is_selectable']
    list_filter = ['depth', 'is_selectable']
    search_fields = ['Name', 'Definition']
    fields = ['URI', 'Name', 'Definition', 'Version', 'ancestors', 'children', 'is_selectable']
    readonly_fields = ['URI', 'Name', 'Definition', 'Version', 'ancestors', 'children', 'is_selectable']

    def ancestors(self, inst):
        list = format_html_join("", "<li><a href='../../{}/'>{}</a></li>",
                                [(node.pk, node.Name,) for node in inst.get_ancestors()])
        return format_html("<ul>{}</ul>", list)

    def children(self, inst):
        # An unfilled node will cause an exception when get_children()
        # is called, so return none when this is the case.
        if inst.tree_id == None:
            return None

        list = format_html_join("", "<li><a href='../../{}/'>{}</a></li>",
                                [(node.pk, node.Name,) for node in inst.get_children()])
        return format_html("<ul>{}</ul>", list)


class ParameterNameAdmin(CommonVocabNodeAdmin):
    pass


class ParameterUnitAdmin(CommonVocabNodeAdmin):
    pass


class ParameterInstrumentAdmin(CommonVocabNodeAdmin):
    pass


class ParameterPlatformAdmin(CommonVocabNodeAdmin):
    pass


class RoleCodeAdmin(admin.ModelAdmin):
    list_display = ['Identifier', 'Description']
    list_filter = ['Identifier', 'Description']
    search_fields = ['Identifier', 'Description']
    fields = ['UUID', 'Identifier', 'Description']
    readonly_fields = ['UUID', 'Identifier', 'Description']


# admin.site.register(models.Institution, InstitutionAdmin)
admin.site.register(models.Document, DocumentAdmin)
admin.site.register(models.DataFeed, DataFeedAdmin)
admin.site.register(models.UserInterfaceTemplate, UserInterfaceTemplateAdmin)
admin.site.register(models.MetadataTemplateMapper, MetadataTemplateMapperAdmin)
admin.site.register(models.MetadataTemplate, MetadataTemplateAdmin)
admin.site.register(models.DocumentAttachment, DocumentAttachmentAdmin)
admin.site.register(models.DraftMetadata, DraftMetadataAdmin)
admin.site.register(models.ScienceKeyword, ScienceKeywordAdmin)
admin.site.register(models.AnzsrcKeyword, AnzsrcKeywordAdmin)
admin.site.register(models.DocumentCategory, DocumentCategoryAdmin)
# admin.site.register(models.ParameterName, ParameterNameAdmin)
# admin.site.register(models.ParameterUnit, ParameterUnitAdmin)
# admin.site.register(models.ParameterInstrument, ParameterInstrumentAdmin)
# admin.site.register(models.ParameterPlatform, ParameterPlatformAdmin)
# admin.site.register(models.RoleCode, RoleCodeAdmin)
# admin.site.register(models.Person, PersonAdmin)
# admin.site.register(models.SamplingFrequency, SamplingFrequencyAdmin)
# admin.site.register(models.HorizontalResolution, HorizontalResolutionAdmin)
# admin.site.register(models.TopicCategory, TopicCategoryAdmin)
