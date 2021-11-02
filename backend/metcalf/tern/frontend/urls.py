from django.conf.urls import include
from django.conf.urls.static import static
from django.urls import path
from rest_framework import routers

import metcalf.tern.backend.viewsets as viewsets
from metcalf.tern.frontend.views import *

router = routers.DefaultRouter()
router.register(r'institution', viewsets.InstitutionViewSet)
router.register(r'sciencekeyword', viewsets.ScienceKeywordViewSet)
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
    path('wp-login.php', home, name="LandingPage"),  # legacy links
    path('portal/home?uuid=<uuid:uuid>/', edit, name="Edit"),  # legacy links
    path('dashboard/', dashboard, name="Dashboard"),
    path('edit/<uuid:uuid>/', edit, name="Edit"),
    path('save/<uuid:uuid>/', save, name="Save"),
    path('transition/<uuid:uuid>/', transition, name="Transition"),
    path('clone/<uuid:uuid>/', clone, name="Clone"),
    path('validation/<uuid:uuid>/', validation_results, name="Validation"),
    path('upload/<uuid:uuid>/', UploadView.as_view(), name="Upload"),
    path('delete/<uuid:uuid>/<int:id>/', delete_attachment, name="DeleteAttachment"),
    path('create/', create, name="Create"),
    path('extract_xml_data/<int:template_id>/', extract_xml_data, name="extract_xml_data"),
    path('analyse_metadata_template/<int:template_id>/', analyse_metadata_template, name="analyse_metadata_template"),
    path('export/<uuid:uuid>/', export, name="Export"),
    path('attachment/<path:path>', download_attachement, name="DownloadAttachment"),
    path('mef/<uuid:uuid>/', mef, name="MEF"),
    path('api/', include(router.urls)),
    path('api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('logout', logout_view, name="Sign Out"),
    path('robots.txt', robots_view, name="Robots"),
    path('api/anzsrckeyword', anzsrc_keywords),
    path('api/ausplantnames', aus_plantnames),
    path('api/gcmdhorizontal', gcmd_horizontal),
    path('api/qudtunits', qudt_units),
    path('api/ternparameters', tern_parameters),
    path('api/ternplatforms', tern_platforms),
    path('api/terninstruments', tern_instruments),
    path('api/terninstrumenttypes', tern_instrument_types),
    path('api/ternpeople', tern_people),
    path('api/ternorgs', tern_orgs),
    path('api/terngeonetwork', geonetwork_entries),

    # Dummy endpoints to be implemented
    path('api/What3', tern_instruments),
    path('api/What4', tern_instruments),
    path('api/What9', tern_instruments),
    path('api/What10', tern_instruments),
    path('api/What11', tern_instruments),
    path('api/What12', tern_instruments),
    path('api/What13', tern_instruments),
    path('api/What14', tern_instruments),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
