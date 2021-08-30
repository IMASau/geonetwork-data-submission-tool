"""
Django settings for IMAS webapp project.
"""

from .settings import *

DEBUG = False

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql_psycopg2",
        "NAME": "postgres",
        "USER": "postgres",
        "PASSWORD": "postgres",
        "HOST": "postgres",
        "PORT": "5432",
        "OPTIONS": {
            "application_name": os.environ.get("HOSTNAME", "data-submission-tool")
        },
    }
}

MEDIA_ROOT = "/data/media"

STATIC_ROOT = "/data/static"

INSTALLED_APPS = [
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.sites",
    "django.contrib.messages",
    "django.contrib.staticfiles",
    "django.contrib.admin",
    "django.contrib.admindocs",
    "rest_framework",
    "django_fsm",
    "fsm_admin",
    "imagekit",
    "backend",
    "frontend.config.FrontendConfig",
    "bootstrap3",
    "mozilla_django_oidc",
]

AUTHENTICATION_BACKENDS = (
    "mozilla_django_oidc.auth.OIDCAuthenticationBackend",
    "django.contrib.auth.backends.ModelBackend",
)

FRONTEND_DEV_MODE = False

ELASTICSEARCH_VERIFY_SSL = True

LOGIN_URL = "/oidc/authenticate"


SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
USE_X_FORWARDED_HOST = True

USE_TERN_STORAGE = False
USE_TERN_AUTH = False
USE_TERN_ELASTIC_SEARCH = False

# HERE STARTS DYNACONF EXTENSION LOAD (Keep at the very bottom of settings.py)
# Read more at https://dynaconf.readthedocs.io/en/latest/guides/django.html
import dynaconf  # noqa

settings = dynaconf.DjangoDynaconf(
    __name__,
    # ENVVAR_PREFIX_FOR_DYNACONF='DST',
    ENVVAR_FOR_DYNACONF="DST_SETTINGS",
    validators=[dynaconf.Validator("ELASTICSEARCH_VERIFY_SSL", is_type_of=bool)],
)  # noqa
# HERE ENDS DYNACONF EXTENSION LOAD (No more code below this line)
