"""
Definition of urls for webapp.
"""
from django.conf import settings
from django.conf.urls import url
from django.contrib import admin
from django.urls import path, include
from django.views.generic import TemplateView

urlpatterns = [
    path('', include('metcalf.tern.frontend.urls')),
    path('', include('metcalf.tern.healthstatus.urls')),
    path('accounts/profile/', TemplateView.as_view(template_name='account/profile.html'), name="account_profile"),
    path('admin/', admin.site.urls),
]

if settings.USE_TERN_AUTH:
    urlpatterns += [
        url(r'^oidc/', include('mozilla_django_oidc.urls')),
    ]
else:
    urlpatterns += [
        path('accounts/', include('django.contrib.auth.urls')),
    ]

handler404 = 'django.views.defaults.page_not_found'
handler500 = 'metcalf.tern.frontend.views.server_error'
handler403 = 'django.views.defaults.permission_denied'
handler400 = 'metcalf.tern.frontend.views.bad_request'
