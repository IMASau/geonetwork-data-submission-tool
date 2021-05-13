"""
Definition of urls for webapp.
"""

from datetime import datetime
from django.urls import include, path
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
    path(r'^', include('frontend.urls')),
    path(r'^accounts/password/change', custom_password_change),
    path(r'^accounts/profile/$', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    path(r'^accounts/', include('allauth.urls')),
    path(r'^admin/doc/', include('django.contrib.admindocs.urls')),
    path(r'^admin/', admin.site.urls),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
