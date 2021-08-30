from django.contrib import admin

from metcalf.imas.frontend.models import SiteContent
from metcalf.imas.frontend.forms import SiteContentForm


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)
