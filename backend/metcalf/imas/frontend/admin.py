from django.contrib import admin

from metcalf.imas.frontend.forms import SiteContentForm
from metcalf.imas.frontend.models import SiteContent


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)
