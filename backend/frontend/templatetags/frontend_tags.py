from django import template
from django.conf import settings

register = template.Library()


@register.simple_tag
def GMAPS_API_KEY():
    return settings.GMAPS_API_KEY


@register.simple_tag
def FRONTEND_DEV_MODE():
    return settings.FRONTEND_DEV_MODE
