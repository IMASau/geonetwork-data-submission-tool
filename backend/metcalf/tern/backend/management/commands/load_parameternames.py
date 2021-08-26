# TODO: when TERN provides a vocab for this, switch away from GCMD keywords
# Uncomment the commented out lines, remove the matching lines
from django.core.management.base import BaseCommand

#from .aodn_helpers import AodnBaseParameterVocabLoader
from .aodn_parametername_helpers import AodnTempBaseParameterVocabLoader

from metcalf.tern.backend.models import ParameterName

#class Command(AodnBaseParameterVocabLoader, BaseCommand):
class Command(AodnTempBaseParameterVocabLoader, BaseCommand):
    help = 'Refresh parameter-name list from online vocab'

    ParameterClass = ParameterName
    #CategoryVocab  = 'aodn-parameter-category-vocabulary'
    #ParameterVocab = 'aodn-discovery-parameter-vocabulary'
    CategoryVocab  = None
    ParameterVocab = 'ardc-curated_gcmd-sciencekeywords_8-6-2018-12-17'
    HumanName      = 'parameter name'
