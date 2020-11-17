from django.apps import AppConfig
from django.conf import settings


class FrontendConfig(AppConfig):
    name = 'frontend'

    def ready(self):
        from elasticsearch_dsl import connections
        connections.create_connection(hosts=[settings.ELASTICSEARCH_URL])