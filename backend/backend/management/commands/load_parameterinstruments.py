from django.core.management.base import BaseCommand

from .helpers import BaseParameterVocabLoader
from backend.models import ParameterInstrument


class Command(BaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-instrument list from online vocab'

    ParameterClass = ParameterInstrument
    CategoryVocab  = None
    ParameterVocab = 'aodn-instrument-vocabulary'
    HumanName      = 'parameter instrument'
