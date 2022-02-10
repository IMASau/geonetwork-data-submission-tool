from django.conf import settings
from django.core.management.base import BaseCommand, CommandError

import requests

from metcalf.tern.backend.serializers import DocumentCategorySerializer


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
            with requests.Session() as session:
                # Retrieve XSRF token:
                res = session.post(f"{gn_root}/srv/eng/info?type=me")
                token = res.cookies['XSRF-TOKEN']
                res = session.get(f"{gn_root}/srv/api/tags",
                                  auth=(gn_user, gn_pass),
                                  headers={'X-XSRF-TOKEN': token,
                                           'Accept': 'application/json'})
                data = list(map(self.extract_eng_label, res.json()))

                serializer = DocumentCategorySerializer(data=data, many=True)
                if serializer.is_valid(raise_exception=True):
                    serializer.save()

        except Exception as e:
            print(e)
            # update model with error
            # raise?
            pass
