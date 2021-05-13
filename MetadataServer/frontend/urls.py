from django.urls import include, path
from rest_framework import routers

from frontend.views import *

router = routers.DefaultRouter()

urlpatterns = (
    path(r'^$', home, name="LandingPage"),
    path(r'^dashboard/$', dashboard, name="Dashboard"),
    path(r'^edit/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', edit, name="Edit"),
    path(r'^transition/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', transition, name="Transition"),
    path(r'^clone/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', clone, name="Clone"),
    path(r'^upload/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', UploadView.as_view(), name="Upload"),
    path(r'^delete/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/(?P<id>\d+)/$', delete_attachment,
        name="DeleteAttachment"),
    path(r'^create/$', create, name="Create"),
    path(r'^theme/$', theme, name="Theme"),
    path(r'^export/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', export, name="Export"),
    path(r'^api/', include(router.urls)),
    path(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework')),
)
