from django.core.management.base import BaseCommand

from .helpers import BaseParameterVocabLoader
from backend.models import ParameterUnit


class Command(BaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-unit list from online vocab'

    ParameterClass = ParameterUnit
    CategoryVocab  = None
    ParameterVocab = 'aodn-units-of-measure-vocabulary'
    HumanName      = 'parameter unit'
