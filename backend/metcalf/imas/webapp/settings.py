"""
Django settings for webapp project.
"""
import os

from django.conf.locale.en import formats as en_formats

en_formats.DATETIME_FORMAT = "Y-m-d H:i:s"

DEBUG = False

DEFAULT_AUTO_FIELD = "django.db.models.AutoField"

EMAIL_BACKEND = "django.core.mail.backends.console.EmailBackend"

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

BLACKLISTED_ATTACHMENT_EXTENSIONS = ["exe", "php"]

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
        "DIRS": ["metcalf/imas/templates", "metcalf/common/templates"],
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
    "django.contrib.sites.middleware.CurrentSiteMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "metcalf.imas.webapp.urls"

WSGI_APPLICATION = "metcalf.imas.webapp.wsgi.application"

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
    "metcalf.imas.backend",
    "metcalf.imas.frontend",
    "allauth",
    "allauth.account",
    "bootstrap3",
    "captcha",
    'django_cleanup.apps.CleanupConfig',
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
        "metcalf.imas.backend.management.commands": {
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
    "PAGE_SIZE": 100,
    "DEFAULT_RENDERER_CLASSES": (
        "rest_framework.renderers.BrowsableAPIRenderer",
        "rest_framework.renderers.JSONRenderer",
        "rest_framework_jsonp.renderers.JSONPRenderer",
        "rest_framework_xml.renderers.XMLRenderer",
    ),
    "EXCEPTION_HANDLER": "metcalf.imas.frontend.utils.custom_exception_handler",
    "SEARCH_PARAM": "query"
}

AUTHENTICATION_BACKENDS = (
    # Needed to login by username in Django admin, regardless of `allauth`
    "django.contrib.auth.backends.ModelBackend",
    "allauth.account.auth_backends.AuthenticationBackend",
)

# We require emails to be verified before we allow any site use -- to
# help cut down on spam behaviour observed
ACCOUNT_EMAIL_REQUIRED = True
ACCOUNT_AUTHENTICATION_METHOD = "username_email"
ACCOUNT_EMAIL_VERIFICATION = "mandatory"
# Custom signup form, to include Recaptcha widgets
ACCOUNT_SIGNUP_FORM_CLASS = 'metcalf.imas.frontend.forms.MySignupForm'
# Recaptcha v3 threshold (based off
# https://recaptcha-demo.appspot.com/recaptcha-v3-request-scores.php I
# am 90% human, so 80% seems reasonable):
RECAPTCHA_REQUIRED_SCORE = 0.5

SITE_ID = 1

GMAPS_API_KEY = ""

FRONTEND_DEV_MODE = False

# Elasticsearch
ELASTICSEARCH_VERIFY_SSL = True

LOGIN_REDIRECT_URL = "/dashboard"
LOGOUT_REDIRECT_URL = "/"

SECURE_PROXY_SSL_HEADER = ("HTTP_X_FORWARDED_PROTO", "https")
USE_X_FORWARDED_HOST = True

GIT_VERSION = os.environ.get("GIT_VERSION", "undefined")

USE_TERN_STORAGE = False
USE_TERN_AUTH = False


SILENCED_SYSTEM_CHECKS = ['captcha.recaptcha_test_key_error']
