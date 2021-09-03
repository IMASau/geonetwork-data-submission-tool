import subprocess

from django import template
from django.conf import settings

register = template.Library()


@register.simple_tag
def GMAPS_API_KEY():
    return settings.GMAPS_API_KEY


@register.simple_tag
def FRONTEND_DEV_MODE():
    return settings.FRONTEND_DEV_MODE


@register.simple_tag
def GIT_VERSION():
    if settings.GIT_VERSION != 'local':
        return settings.GIT_VERSION
    try:
        p = subprocess.Popen(["git", "show", "HEAD", "--no-patch", "--no-notes", "--date=short", '--pretty=%h %cd'],
                             stdout=subprocess.PIPE)
        gv = p.communicate()[0]
        return gv.strip().decode('UTF-8')

    except:
        return '(unknown)'
