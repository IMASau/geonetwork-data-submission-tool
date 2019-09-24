from django.core.management.base import BaseCommand

from .aodn_helpers import AodnBaseParameterVocabLoader
from backend.models import ParameterUnit

# TODO: when TERN provides a vocab for this, switch away from AODN commands
class Command(AodnBaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-unit list from online vocab'

    ParameterClass = ParameterUnit
    CategoryVocab  = None
    ParameterVocab = 'aodn-units-of-measure-vocabulary'
    HumanName      = 'parameter unit'
