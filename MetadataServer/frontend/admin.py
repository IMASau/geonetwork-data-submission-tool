from django.contrib import admin

from frontend.models import SiteContent
from frontend.forms import SiteContentForm


class SiteContentAdmin(admin.ModelAdmin):
    list_display = ['pk', 'title', 'tag_line', 'email']
    form = SiteContentForm


admin.site.register(SiteContent, SiteContentAdmin)
