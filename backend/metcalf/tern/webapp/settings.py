"""
Django settings for webapp project.
"""
import os

from django.conf.locale.en import formats as en_formats

en_formats.DATETIME_FORMAT = "Y-m-d H:i:s"

DEBUG = False

DEFAULT_AUTO_FIELD = 'django.db.models.AutoField'

EMAIL_BACKEND = "django.core.mail.backends.console.EmailBackend"
# EMAIL_HOST = os.environ.get("DJANGO_EMAIL_HOST")
# EMAIL_HOST_USER = os.environ.get('DJANGO_EMAIL_HOST_USER', 'no-reply@tern.org.au')
# EMAIL_HOST_PASSWORD = os.environ.get("DJANGO_EMAIL_HOST_PASSWORD")
# EMAIL_PORT = int(os.environ.get('DJANGO_EMAIL_PORT', 587))
# EMAIL_USE_TLS = strtobool(os.environ.get('DJANGO_EMAIL_USE_TLS', 'True').lower())
# DEFAULT_FROM_EMAIL = os.environ.get('DJANGO_DEFAULT_FROM_EMAIL', 'no-reply@tern.org.au')

ADMINS = []

INTERNAL_IPS = []

ALLOWED_HOSTS = ["*"]

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

METCALF_FROM_EMAIL = ""

TIME_ZONE = "Australia/Hobart"

LANGUAGE_CODE = "en-us"

USE_I18N = True

USE_L10N = True

USE_TZ = True

MEDIA_URL = "/media/"
MEDIA_ROOT = "/data/media"

STATIC_URL = "/static/"
STATIC_ROOT = "/data/static"

AUTH_PASSWORD_VALIDATORS = [
    {
        "NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator",
    },
    {"NAME": "django.contrib.auth.password_validation.MinimumLengthValidator", },
    {"NAME": "django.contrib.auth.password_validation.CommonPasswordValidator", },
    {"NAME": "django.contrib.auth.password_validation.NumericPasswordValidator", },
]

SECRET_KEY = "get-a-new-key"

TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": ["metcalf/tern/templates"],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.debug",
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

MIDDLEWARE = [
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.middleware.csrf.CsrfViewMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "metcalf.tern.webapp.urls"

WSGI_APPLICATION = "metcalf.tern.webapp.wsgi.application"

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
    "metcalf.tern.backend",
    "metcalf.tern.frontend.config.FrontendConfig",
    "bootstrap3",
    "mozilla_django_oidc",
]

LOGGING = {
    "version": 1,
    "disable_existing_loggers": False,
    "filters": {"require_debug_false": {"()": "django.utils.log.RequireDebugFalse"}},
    "formatters": {
        "verbose": {
            "format": "%(levelname)s %(asctime)s %(module)s %(process)d %(thread)d %(message)s"
        },
        "simple": {"format": "%(levelname)s %(message)s"},
    },
    "handlers": {
        "mail_admins": {
            "level": "ERROR",
            "filters": ["require_debug_false"],
            "class": "django.utils.log.AdminEmailHandler",
        },
        "console": {
            "level": "DEBUG",
            "class": "logging.StreamHandler",
            "formatter": "simple",
        },
    },
    "loggers": {
        "django.request": {
            "handlers": ["mail_admins"],
            "level": "ERROR",
            "propagate": True,
        },
        "metcalf.common.xmlutils": {
            "handlers": ["console", "mail_admins"],
            "level": "DEBUG",
            "propagate": True,
        },
        "metcalf.tern.backend.management.commands": {
            "handlers": ["console", "mail_admins"],
            "level": "DEBUG",
            "propagate": True,
        },
        "django": {"handlers": ["console"], "level": "INFO", "propagate": True, },
    },
}

TEST_RUNNER = "django.test.runner.DiscoverRunner"

REST_FRAMEWORK = {
    "DEFAULT_PERMISSION_CLASSES": ("rest_framework.permissions.IsAuthenticated",),
    "DEFAULT_FILTER_BACKENDS": (
        "django_filters.rest_framework.DjangoFilterBackend",
        "rest_framework.filters.SearchFilter",
    ),
    "DEFAULT_PAGINATION_CLASS": "rest_framework.pagination.LimitOffsetPagination",
    "PAGE_SIZE": 10000,
    "DEFAULT_RENDERER_CLASSES": (
        "rest_framework.renderers.BrowsableAPIRenderer",
        "rest_framework.renderers.JSONRenderer",
        "rest_framework_jsonp.renderers.JSONPRenderer",
        "rest_framework_xml.renderers.XMLRenderer",
    ),
    "EXCEPTION_HANDLER": "metcalf.tern.frontend.utils.custom_exception_handler",
}

AUTHENTICATION_BACKENDS = (
    "mozilla_django_oidc.auth.OIDCAuthenticationBackend",
    "django.contrib.auth.backends.ModelBackend",
)

ACCOUNT_AUTHENTICATION_METHOD = "username_email"

SITE_ID = 1

ACCOUNT_EMAIL_REQUIRED = True

GMAPS_API_KEY = ""

FRONTEND_DEV_MODE = False

# Elasticsearch
ELASTICSEARCH_VERIFY_SSL = True

# LOGIN_URL = "/accounts/login/"

# OIDC_OP_AUTHORIZATION_ENDPOINT = os.environ.get("OIDC_OP_AUTHORIZATION_ENDPOINT")
# OIDC_OP_TOKEN_ENDPOINT = os.environ.get("OIDC_OP_TOKEN_ENDPOINT")
# OIDC_OP_USER_ENDPOINT = os.environ.get("OIDC_OP_USER_ENDPOINT")
# OIDC_RP_CLIENT_ID = os.environ.get("OIDC_RP_CLIENT_ID")
# OIDC_OP_JWKS_ENDPOINT = os.environ.get("OIDC_OP_JWKS_ENDPOINT")
# OIDC_LOGOUT_ENDPOINT = os.environ.get("OIDC_LOGOUT_ENDPOINT")
# OIDC_RP_CLIENT_SECRET = os.environ.get("OIDC_RP_CLIENT_SECRET")
# OIDC_RP_SIGN_ALGO = os.environ.get("OIDC_RP_SIGN_ALGO", "RS256")
LOGIN_REDIRECT_URL = "/dashboard"
LOGOUT_REDIRECT_URL = "/"

SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
USE_X_FORWARDED_HOST = True

GIT_VERSION = os.environ.get("GIT_VERSION", "undefined")

USE_TERN_STORAGE = True
USE_TERN_AUTH = True

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
