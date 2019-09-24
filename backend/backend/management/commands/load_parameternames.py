from django.core.management.base import BaseCommand

from .aodn_helpers import AodnBaseParameterVocabLoader
from backend.models import ParameterName

# TODO: when TERN provides a vocab for this, switch away from AODN commands
class Command(AodnBaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-name list from online vocab'

    ParameterClass = ParameterName
    CategoryVocab  = 'aodn-parameter-category-vocabulary'
    ParameterVocab = 'aodn-discovery-parameter-vocabulary'
    HumanName      = 'parameter name'
