from django.contrib import admin

from metcalf.tern.frontend.forms import SiteContentForm
from metcalf.tern.frontend.models import SiteContent


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)
