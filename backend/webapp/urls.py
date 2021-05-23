"""
Definition of urls for webapp.
"""

from datetime import datetime
from django.urls import include, re_path
from django.conf.urls.static import static

from django.conf import settings

from django.contrib import admin
from django.contrib.auth.decorators import login_required
from django.views.generic import TemplateView
import allauth.account.views as allauth_views
from django.urls import reverse, reverse_lazy


class CustomPasswordChangeView(allauth_views.PasswordChangeView):
    success_url = reverse_lazy("account_profile")

custom_password_change = login_required(CustomPasswordChangeView.as_view())

admin.autodiscover()

urlpatterns = [
    re_path(r'^', include('frontend.urls')),
    re_path(r'^accounts/password/change', custom_password_change),
    re_path(r'^accounts/profile/$', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    re_path(r'^accounts/', include('allauth.urls')),
    re_path(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    re_path(r'^admin/', admin.site.urls),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
