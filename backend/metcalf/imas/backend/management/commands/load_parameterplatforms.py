from django.core.management.base import BaseCommand

from .helpers import BaseParameterVocabLoader
from metcalf.imas.backend.models import ParameterPlatform


class Command(BaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-platform list from online vocab'

    ParameterClass = ParameterPlatform
    CategoryVocab = 'aodn-platform-category-vocabulary'
    ParameterVocab = 'aodn-platform-vocabulary'
    HumanName = 'parameter platform'
