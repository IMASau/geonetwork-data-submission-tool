from django.core.management.base import BaseCommand

from .helpers import BaseParameterVocabLoader
from metcalf.imas.backend.models import ParameterPlatform


class Command(BaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-platform list from online vocab'

    ParameterClass = ParameterPlatform
    CategoryVocab  = None
    TopCategory = 'http://linkeddata.tern.org.au/def/platforms/d55be420-154a-4ec6-96f2-43198d14cb71'
    ParameterVocab = 'tern-rva_platform-type_0-0-2'
    HumanName      = 'parameter platform'
