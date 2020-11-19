from django.apps import AppConfig
from django.conf import settings

from elasticsearch_dsl import connections


def init_elasticsearch():
    if hasattr(settings, 'ELASTICSEARCH_VERIFY_SSL'):
        # Can be set to either true or false in the environment variable.
        verify_ssl = settings.ELASTICSEARCH_VERIFY_SSL
    else:
        # If not set, default to true.
        verify_ssl = True

    connections.create_connection(
        hosts=[settings.ELASTICSEARCH_URL],
        http_auth=(
            settings.ELASTICSEARCH_USER,
            settings.ELASTICSEARCH_PASSWORD
        ),
        verify_ssl=verify_ssl
    )


class FrontendConfig(AppConfig):
    name = 'frontend'

    def ready(self):
        init_elasticsearch()
