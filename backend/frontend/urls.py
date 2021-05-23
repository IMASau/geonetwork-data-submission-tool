from django.urls import include, re_path
from rest_framework import routers

from frontend.views import *

router = routers.DefaultRouter()

urlpatterns = (
    re_path(r'^$', home, name="LandingPage"),
    re_path(r'^dashboard/$', dashboard, name="Dashboard"),
    re_path(r'^edit/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', edit, name="Edit"),
    re_path(r'^transition/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', transition, name="Transition"),
    re_path(r'^clone/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', clone, name="Clone"),
    re_path(r'^upload/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', UploadView.as_view(), name="Upload"),
    re_path(r'^delete/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/(?P<id>\d+)/$', delete_attachment,
        name="DeleteAttachment"),
    re_path(r'^create/$', create, name="Create"),
    re_path(r'^theme/$', theme, name="Theme"),
    re_path(r'^export/(?P<uuid>\w{8}-\w{4}-\w{4}-\w{4}-\w{12})/$', export, name="Export"),
    re_path(r'^api/', include(router.urls)),
    re_path(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework')),
)
