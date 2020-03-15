from django.conf.urls import include
from django.conf.urls.static import static
from django.urls import path
from rest_framework import routers

import backend.viewsets as viewsets
from frontend.views import *

router = routers.DefaultRouter()
router.register(r'institution', viewsets.InstitutionViewSet)
router.register(r'sciencekeyword', viewsets.ScienceKeywordViewSet)
router.register(r'anzsrckeyword', viewsets.AnzsrcKeywordViewSet)
router.register(r'rolecode', viewsets.RoleCodeViewSet)
router.register(r'parametername', viewsets.ParameterNameViewSet)
router.register(r'parameterunit', viewsets.ParameterUnitViewSet)
router.register(r'parameterinstrument', viewsets.ParameterInstrumentViewSet)
router.register(r'parameterplatform', viewsets.ParameterPlatformViewSet)
router.register(r'person', viewsets.PersonViewSet)
router.register(r'samplingfrequency', viewsets.SamplingFrequencyViewSet)
router.register(r'horizontalresolution', viewsets.HorizontalResolutionViewSet)
router.register(r'topiccategory', viewsets.TopicCategoryViewSet)

urlpatterns = [
    path('', home, name="LandingPage"),
    path('dashboard/', dashboard, name="Dashboard"),
    path('edit/<uuid:uuid>/', edit, name="Edit"),
    path('save/<uuid:uuid>/', save, name="Save"),
    path('transition/<uuid:uuid>/', transition, name="Transition"),
    path('clone/<uuid:uuid>/', clone, name="Clone"),
    path('validation/<uuid:uuid>/', validation_results, name="Validation"),
    path('upload/<uuid:uuid>/', UploadView.as_view(), name="Upload"),
    path('delete/<uuid:uuid>/<int:id>/', delete_attachment, name="DeleteAttachment"),
    path('create/', create, name="Create"),
    path('theme/', theme, name="Theme"),
    path('export/<uuid:uuid>/', export, name="Export"),
    path('attachment/<path:path>', download_attachement, name="DownloadAttachment"),
    path('mef/<uuid:uuid>/', mef, name="MEF"),
    path('api/', include(router.urls)),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('logout', logout_view, name="Sign Out"),
    path('robots.txt', robots_view, name="Robots"),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
