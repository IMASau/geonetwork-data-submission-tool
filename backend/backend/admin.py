from django.contrib import admin
from django.urls import reverse
from django.utils.html import format_html, format_html_join
from fsm_admin.mixins import FSMTransitionMixin

from backend import models


class InstitutionAdmin(admin.ModelAdmin):
    list_display = ['prefLabel', 'organisationName', 'deliveryPoint', 'city', 'administrativeArea', 'postalCode',
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
    list_display = ['familyName', 'givenName', 'honorificPrefix', 'prefLabel', 'electronicMailAddress', 'orcid', 'organisation', 'isUserAdded']
    search_fields = ['familyName', 'givenName', 'orcid']

    def organisation(self, obj):
        org = models.Institution.objects.filter(uri=obj.orgUri).first()
        if org is None:
            return format_html('<a href="{0}" target="_blank">{0}</a>', obj.orgUri)
        else:
            return org.organisationName

class SamplingFrequencyAdmin(admin.ModelAdmin):
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
        if q is "Good":
            return format_html("<b>{0}</b> - {1}", "Good", "Most recent refresh succeeded")
        elif q is "Stale":
            return format_html("<b>{0}</b> - {1}", "Stale", "We have data but the most recent refresh failed")
        elif q is "Bad":
            return format_html("<b>{0}</b> - {1}", "Bad", "We have not successfully refreshed data")
        elif q is "Uninitialised":
            return format_html("<b>{0}</b> - {1}", "No data", "No refresh has been attempted")
        else:
            return format_html("<b>{0}</b>", q)

    feed_quality.short_description = "Feed quality"


class DocumentAdmin(FSMTransitionMixin, admin.ModelAdmin):
    list_display = ['__str__', 'owner', 'template', 'status', 'action_links']
    list_filter = ['status', 'template']
    search_fields = ['title', 'owner__username', 'uuid']
    fsm_field = ['status', ]
    readonly_fields = ['status', 'action_links', 'submission_note']
    inlines = [DocumentAttachmentInline]
    fieldsets = [
        (None, {'fields': ('title', 'template', 'owner', 'status', 'submission_note')}),
        ('Export', {'fields': ('action_links',)}),
    ]

    def submission_note(self, obj):
        if obj.latest_draft:
            return obj.latest_draft.noteForDataManager

    def action_links(self, obj):
        return format_html("<a href='{0}' target='_blank'>Edit</a> | "
                           "<a href='{1}' target='_blank'>Export XML</a> | "
                           "<a href='{2}' target='_blank'>Export MEF</a>",
                           reverse('Edit', kwargs={'uuid': obj.uuid}),
                           reverse('Export', kwargs={'uuid': obj.uuid}),
                           reverse('MEF', kwargs={'uuid': obj.uuid}))

    action_links.short_description = "Actions"


class DocumentAttachmentAdmin(admin.ModelAdmin):
    list_display = ['document', 'file']


class ScienceKeywordAdmin(admin.ModelAdmin):
    list_display = ['UUID', 'Category', 'Topic', 'Term',
                    'VariableLevel1', 'VariableLevel2', 'VariableLevel3',
                    'DetailedVariable']
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
        if inst.tree_id is None:
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
admin.site.register(models.ParameterName, ParameterNameAdmin)
admin.site.register(models.ParameterUnit, ParameterUnitAdmin)
admin.site.register(models.ParameterInstrument, ParameterInstrumentAdmin)
admin.site.register(models.ParameterPlatform, ParameterPlatformAdmin)
admin.site.register(models.RoleCode, RoleCodeAdmin)
admin.site.register(models.Person, PersonAdmin)
admin.site.register(models.SamplingFrequency, SamplingFrequencyAdmin)
admin.site.register(models.TopicCategory, TopicCategoryAdmin)