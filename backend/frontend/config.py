from django.apps import AppConfig
from django.conf import settings

from elasticsearch_dsl import connections


class FrontendConfig(AppConfig):
    name = 'frontend'

    def ready(self):
        connections.create_connection(
            hosts=[settings.ELASTICSEARCH_URL],
            http_auth=(
                settings.ELASTICSEARCH_USER,
                settings.ELASTICSEARCH_PASSWORD
            ),
            verify_ssl=False
        )