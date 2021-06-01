from django.apps import AppConfig
from django.conf import settings

if settings.USE_TERN_ELASTICSEARCH:
    from elasticsearch_dsl import connections


    def init_elasticsearch():
        connections.create_connection(
            hosts=[settings.ELASTICSEARCH_URL],
            http_auth=(settings.ELASTICSEARCH_USER, settings.ELASTICSEARCH_PASSWORD),
            verify_certs=settings.ELASTICSEARCH_VERIFY_SSL,
        )


class FrontendConfig(AppConfig):
    name = "frontend"

    def ready(self):
        if settings.USE_TERN_ELASTICSEARCH:
            init_elasticsearch()
