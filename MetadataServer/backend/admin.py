from django.contrib import admin
from django.urls import reverse
from django.utils.html import format_html
from fsm_admin.mixins import FSMTransitionMixin

from backend import models


class InstitutionAdmin(admin.ModelAdmin):
    list_display = ['organisationName', 'deliveryPoint', 'city', 'administrativeArea', 'postalCode', 'country']
    search_fields = ['organisationName', 'deliveryPoint', 'deliveryPoint2', 'city', 'administrativeArea', 'postalCode',
                     'country']
    list_filter = ['country']


class DraftMetadataAdmin(admin.ModelAdmin):
    list_display = ['time', 'user', 'document']
    list_filter = ['time', 'user']
    search_fields = ['data', 'document__pk']


class MetadataTemplateAdmin(admin.ModelAdmin):
    list_display = ['name', 'file', 'site', 'notes']
    list_filter = ['archived', 'site', 'created', 'modified']
    ordering = ['modified']
    readonly_fields = ['created', 'modified']


class DocumentAttachmentInline(admin.TabularInline):
    model = models.DocumentAttachment


class DocumentAdmin(FSMTransitionMixin, admin.ModelAdmin):
    list_display = ['__unicode__', 'owner', 'template', 'status', 'action_links']
    list_filter = ['status', 'template']
    search_fields = ['title', 'owner__username', 'uuid']
    fsm_field = ['status', ]
    readonly_fields = ['status', 'action_links']
    inlines = [DocumentAttachmentInline]
    fieldsets = [
        (None, {'fields': ('title', 'template', 'owner', 'status')}),
        ('Export', {'fields': ('action_links',)}),
    ]

    def action_links(self, obj):
        return format_html("<a href='{0}' target='_blank'>Edit</a> | "
                           "<a href='{1}' target='_blank'>Export</a> ",
                           reverse('Edit', kwargs={'uuid': obj.uuid}),
                           reverse('Export', kwargs={'uuid': obj.uuid}))

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


admin.site.register(models.Institution, InstitutionAdmin)
admin.site.register(models.Document, DocumentAdmin)
admin.site.register(models.MetadataTemplate, MetadataTemplateAdmin)
admin.site.register(models.DocumentAttachment, DocumentAttachmentAdmin)
admin.site.register(models.DraftMetadata, DraftMetadataAdmin)
admin.site.register(models.ScienceKeyword, ScienceKeywordAdmin)
