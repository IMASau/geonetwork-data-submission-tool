import json

from django.db import connection
from django.http import JsonResponse
from elasticsearch_dsl.connections import connections
from healthcheck import HealthCheck
from rest_framework.decorators import api_view
import requests

from metcalf.tern.webapp.settings import settings

# Create your views here.
health = HealthCheck()

OK_STATUS = 'ok'
FAILED_STATUS = 'unreachable'


def check_elasticsearch_index_health(index):
    es = connections.get_connection()
    body = {"size": 0}
    try:
        response = es.search(index=index, body=body, timeout='5s')
        if not response['timed_out']:
            return True, OK_STATUS
        else:
            return False, 'check timed out'
    except Exception as e:
        return False, str(e)


def check_database_health():
    try:
        c = connection.cursor()
        c.execute('SELECT 1')
    except Exception as e:
        return False, str(e)
    return True, OK_STATUS


def check_geonetwork_health():
    response = requests.get('https://metadata.imas.utas.edu.au/geonetwork/criticalhealthcheck', timeout=5)
    if response.status_code == 200:
        return True, OK_STATUS
    else:
        return False, response.text


for setting in settings:
    if "ELASTICSEARCH_INDEX" in setting:
        index_name = settings[setting]
        checkerfn = lambda index_name=index_name: check_elasticsearch_index_health(index_name)
        # for the py-healthcheck library to display the checker name
        checkerfn.__name__ = 'es_' + index_name
        health.add_check(checkerfn)
health.add_check(check_database_health)
health.add_check(check_geonetwork_health)


@api_view(["GET", "POST"])
def check_health(request) -> JsonResponse:
    resp = health.run()
    return JsonResponse(json.loads(resp[0]), status=resp[1])
