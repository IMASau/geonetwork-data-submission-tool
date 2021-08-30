"""
Definition of urls for webapp.
"""
from django.conf import settings
from django.contrib import admin
from django.urls import path, include
from django.conf.urls import url
from django.views.generic import TemplateView

urlpatterns = [
    path('', include('metcalf.imas.frontend.urls')),
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

handler404 = 'metcalf.imas.frontend.views.not_found'
handler500 = 'metcalf.imas.frontend.views.server_error'
handler403 = 'metcalf.imas.frontend.views.permission_denied'
handler400 = 'metcalf.imas.frontend.views.bad_request'