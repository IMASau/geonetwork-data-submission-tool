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
