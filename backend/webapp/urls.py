"""
Definition of urls for webapp.
"""
from django.contrib import admin
from django.urls import path, include
from django.conf.urls import url
from django.views.generic import TemplateView

urlpatterns = [
    path('', include('frontend.urls')),
    path('accounts/profile/', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    path('admin/', admin.site.urls),
    url(r'^oidc/', include('mozilla_django_oidc.urls')),
]

handler404 = 'frontend.views.not_found'
handler500 = 'frontend.views.server_error'
handler403 = 'frontend.views.permission_denied'
handler400 = 'frontend.views.bad_request'