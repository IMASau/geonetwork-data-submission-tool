from django.contrib import admin

from metcalf.imas.frontend.forms import SiteContentForm
from metcalf.imas.frontend.models import SiteContent, DataSource


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)


class DataSourceAdmin(admin.ModelAdmin):
    list_display = [
        'slug',
        'base_url',
        'search_param',
        'response_key',
    ]


admin.site.register(DataSource, DataSourceAdmin)
