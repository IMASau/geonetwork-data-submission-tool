from django.contrib import admin

from frontend.forms import SiteContentForm
from frontend.models import SiteContent


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)
