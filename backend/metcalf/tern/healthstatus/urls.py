from django.urls import path

from metcalf.tern.healthstatus.views import check_health

urlpatterns = [
    path('sys/healthcheck', check_health),
]

