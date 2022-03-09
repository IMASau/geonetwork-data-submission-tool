from django.conf.urls import include
from django.conf.urls.static import static
from django.urls import path
from rest_framework import routers

import metcalf.imas.backend.viewsets as viewsets
from metcalf.imas.frontend.views import *
from metcalf.imas.frontend.viewsets import DocumentInfoViewSet

router = routers.DefaultRouter()
router.register(r'institution', viewsets.InstitutionViewSet)
# router.register(r'sciencekeyword', viewsets.ScienceKeywordViewSet)
# router.register(r'anzsrckeyword', viewsets.AnzsrcKeywordViewSet)
# router.register(r'rolecode', viewsets.RoleCodeViewSet)
router.register(r'parametername', viewsets.ParameterNameViewSet)
router.register(r'parameterunit', viewsets.ParameterUnitViewSet)
router.register(r'parameterinstrument', viewsets.ParameterInstrumentViewSet)
router.register(r'parameterplatform', viewsets.ParameterPlatformViewSet)
# router.register(r'person', viewsets.PersonViewSet)
# router.register(r'samplingfrequency', viewsets.SamplingFrequencyViewSet)
# router.register(r'horizontalresolution', viewsets.HorizontalResolutionViewSet)
# router.register(r'topiccategory', viewsets.TopicCategoryViewSet)
router.register(r'metadata-template', viewsets.MetadataTemplateViewSet)
router.register(r'document-info', DocumentInfoViewSet)

urlpatterns = [
    path('', home, name="LandingPage"),
    path('portal/home?uuid=<uuid:uuid>/', edit, name="Edit"),  # legacy link
    path('dashboard/', dashboard, name="Dashboard"),
    path('edit/<uuid:uuid>/', edit, name="Edit"),
    path('save/<uuid:uuid>/<int:update_number>/', save, name="Save"),
    path('transition/<uuid:uuid>/', transition, name="Transition"),
    path('clone/<uuid:uuid>/', clone, name="Clone"),
    path('share/<uuid:uuid>/', share, name="share"),
    path('unshare/<uuid:uuid>/', unshare, name="unshare"),
    path('upload/<uuid:uuid>/', UploadView.as_view(), name="Upload"),
    path('delete/<uuid:uuid>/<int:id>/', delete_attachment, name="DeleteAttachment"),
    path('create/', create, name="Create"),
    path('extract_xml_data/<int:template_id>/', extract_xml_data, name="extract_xml_data"),
    path('extract_xml_data2/<int:template_id>/', extract_xml_data2, name="extract_xml_data"),
    path('analyse_metadata_template/<int:template_id>/', analyse_metadata_template, name="analyse_metadata_template"),
    path('export/<uuid:uuid>/', export, name="Export"),
    path('export2/<uuid:uuid>/', export2, name="Export2"),
    path('attachment/<path:path>', download_attachement, name="DownloadAttachment"),
    path('mef/<uuid:uuid>/', mef, name="MEF"),
    path('api/', include(router.urls)),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('robots.txt', robots_view, name="Robots"),
    path('api/keywords_with_breadcrumb_info', keywords_with_breadcrumb_info),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
