"""
Definition of urls for webapp.
"""

from datetime import datetime
from django.conf.urls import patterns, url
from django.conf.urls.static import static

from django.conf import settings

from django.conf.urls import include
from django.contrib import admin
from django.contrib.auth.decorators import login_required
from django.views.generic import TemplateView
import allauth.account.views as allauth_views
from django.urls import reverse, reverse_lazy


class CustomPasswordChangeView(allauth_views.PasswordChangeView):
    success_url = reverse_lazy("account_profile")

custom_password_change = login_required(CustomPasswordChangeView.as_view())

admin.autodiscover()

urlpatterns = patterns('',
    url(r'^', include('frontend.urls')),
    url(r'^accounts/password/change', custom_password_change),
    url(r'^accounts/profile/$', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    url(r'^accounts/', include('allauth.urls')),
    url(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    url(r'^admin/', include(admin.site.urls)),
)

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
