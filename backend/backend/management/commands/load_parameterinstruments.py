from django.core.management.base import BaseCommand

from backend.models import ParameterInstrument
from .aodn_helpers import AodnBaseParameterVocabLoader


# TODO: when TERN provides a vocab for this, switch away from AODN commands
class Command(AodnBaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-instrument list from online vocab'

    ParameterClass = ParameterInstrument
    CategoryVocab = None
    ParameterVocab = 'aodn-instrument-vocabulary'
    HumanName = 'parameter instrument'
