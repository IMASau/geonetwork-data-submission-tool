"""
WSGI config for webapp project.

It exposes the WSGI callable as a module-level variable named ``application``.

For more information on this file, see
https://docs.djangoproject.com/en/2.1/howto/deployment/wsgi/
"""
import os

from django.core.wsgi import get_wsgi_application

# load default settings, which then will include other options loaded via
# django-split-settings
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'metcalf.tern.webapp.settings')

application = get_wsgi_application()
