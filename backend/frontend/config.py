from django.apps import AppConfig
from django.conf import settings

from elasticsearch_dsl import connections


class FrontendConfig(AppConfig):
    name = "frontend"

    def ready(self):
        connections.create_connection(
            hosts=[settings.ELASTICSEARCH_URL],
            verify_ssl=settings.ELASTICSEARCH_VERIFY_SSL,
        )
