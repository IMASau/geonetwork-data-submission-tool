import logging

from django.conf import settings
from django.core.management.base import BaseCommand, CommandError

import requests

from metcalf.tern.backend.models import DocumentCategory
from metcalf.tern.backend.serializers import DocumentCategorySerializer

logger = logging.getLogger(__name__)


class Command(BaseCommand):
    help = "Load document categories (tags) from Geonetwork. Run via a cron job."

    def extract_eng_label(self, category):
        category['label'] = category['label']['eng']
        return category

    def handle(self, *names, **options):
        gn_root = settings.GEONETWORK_URLROOT
        gn_user = settings.GEONETWORK_USER
        gn_pass = settings.GEONETWORK_PASSWORD
        try:
            res = requests.get(f"{gn_root}/srv/api/tags",
                               auth=(gn_user, gn_pass),
                               headers={'Accept': 'application/json'})
            data = list(map(self.extract_eng_label, res.json()))

            existing_categories = DocumentCategory.objects.all()
            serializer = DocumentCategorySerializer(existing_categories, data=data, many=True)
            if serializer.is_valid(raise_exception=True):
                serializer.save()

        except Exception as e:
            import traceback
            logger.error(traceback.format_exc())
            raise
