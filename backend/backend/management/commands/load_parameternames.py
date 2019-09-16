from django.core.management.base import BaseCommand

from .helpers import BaseParameterVocabLoader
from backend.models import ParameterName


class Command(BaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-name list from online vocab'

    ParameterClass = ParameterName
    CategoryVocab  = 'aodn-parameter-category-vocabulary'
    ParameterVocab = 'aodn-discovery-parameter-vocabulary'
    HumanName      = 'parameter name'
