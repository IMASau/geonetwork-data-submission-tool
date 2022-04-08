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

from metcalf.imas.backend import models
from metcalf.imas.frontend.models import SiteContent


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
    list_display = ['admin_name', 'owner_name', 'status', 'action_links']
    list_filter = ['status', 'template']
    search_fields = ['title', 'owner__username', 'owner__email', 'uuid']
    fsm_field = ['status', ]
    readonly_fields = ['status', 'action_links', 'submission_note', 'doi_minting']
    inlines = [DocumentAttachmentInline]
    autocomplete_fields=['contributors']
    fieldsets = [
        (None, {'fields': ('title', 'template', 'owner', 'contributors', 'status', 'submission_note', 'doi_minting')}),
        ('Export', {'fields': ('action_links',)}),
    ]

    # a quick hack to make admin interface a bit nicer to use for title field
    formfield_overrides = {
        django_models.TextField: {'widget': widgets.AdminTextInputWidget(attrs={'style': "width:60em;"})}
    }

    def submission_note(self, obj):
        if obj.latest_draft:
            return obj.latest_draft.noteForDataManager

    @admin.display(description='DOI Minting')
    def doi_minting(self, obj):
        try:
            wantsDoi = obj.latest_draft.doiRequested
            if not wantsDoi:
                return "User has not requested a DOI be minted."
            else:
                if obj.status != models.Document.SUBMITTED:
                    return "Cannot mint a DOI until the document is lodged."
                else:
                    return "User has requested that a DOI be minted."
        except Exception:
            return "Cannot mint a DOI for this document"

    def action_links(self, obj):
        htmlString = ("<a href='{0}' target='_blank'>Edit</a> | "
                      "<a href='{1}?download' target='_blank'>Export XML</a>")
        replacements = [reverse('Edit', kwargs={'uuid': obj.uuid}),
                        reverse('Export', kwargs={'uuid': obj.uuid})]
        return format_html(htmlString, *replacements)

    def owner_name(self, obj):
        if obj.owner.email:
            return obj.owner.email
        return obj.owner.username

    def admin_name(self, obj):
        return "{1} ({0})".format(str(obj.uuid)[:8], obj.short_title())

    action_links.short_description = "Actions"
    owner_name.admin_order_field = 'owner__email'
    admin_name.admin_order_field = 'title'


class DocumentAttachmentAdmin(admin.ModelAdmin):
    list_display = ['document', 'file']


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


class GeographicExtentKeywordAdmin(admin.ModelAdmin):
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


admin.site.register(models.Institution, InstitutionAdmin)
admin.site.register(models.Document, DocumentAdmin)
admin.site.register(models.DataFeed, DataFeedAdmin)
admin.site.register(models.MetadataTemplateMapper, MetadataTemplateMapperAdmin)
admin.site.register(models.MetadataTemplate, MetadataTemplateAdmin)
admin.site.register(models.DocumentAttachment, DocumentAttachmentAdmin)
admin.site.register(models.DraftMetadata, DraftMetadataAdmin)
admin.site.register(models.ScienceKeyword, ScienceKeywordAdmin)
admin.site.register(models.AnzsrcKeyword, AnzsrcKeywordAdmin)
admin.site.register(models.GeographicExtentKeyword, GeographicExtentKeywordAdmin)
admin.site.register(models.ParameterName, ParameterNameAdmin)
admin.site.register(models.ParameterUnit, ParameterUnitAdmin)
admin.site.register(models.ParameterInstrument, ParameterInstrumentAdmin)
admin.site.register(models.ParameterPlatform, ParameterPlatformAdmin)
admin.site.register(models.RoleCode, RoleCodeAdmin)
admin.site.register(models.Person, PersonAdmin)
admin.site.register(models.SamplingFrequency, SamplingFrequencyAdmin)
admin.site.register(models.HorizontalResolution, HorizontalResolutionAdmin)
admin.site.register(models.TopicCategory, TopicCategoryAdmin)
