import requests
from django.db import connection
from django.http import JsonResponse
from rest_framework.response import Response
from rest_framework.decorators import api_view
from elasticsearch_dsl.connections import connections
# Create your views here.
from healthcheck import HealthCheck
from metcalf.tern.webapp.settings import settings
import json

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
    except Exception as e:
        pass
    return False, FAILED_STATUS


def check_database_health():
    c = connection.cursor()
    try:
        c.execute('SELECT 1')
    except Exception:
        return False, FAILED_STATUS
    return True, OK_STATUS


def check_geonetwork_health():
    response = requests.get('https://metadata.imas.utas.edu.au/geonetwork/criticalhealthcheck')
    if response.status_code == 200:
        return True, OK_STATUS
    else:
        return False, FAILED_STATUS


for setting in settings:
    if "ELASTICSEARCH_INDEX" in setting:
        index_name = settings[setting]
        checkerfn = lambda index_name=index_name: check_elasticsearch_index_health(index_name)
        # for the py-healthcheck library to recognize the health
        checkerfn.__name__ = 'es_' + index_name
        health.add_check(checkerfn)
health.add_check(check_database_health)
health.add_check(check_geonetwork_health)


@api_view(["GET", "POST"])
def check_health(request) -> JsonResponse:
    resp = health.run()
    return JsonResponse(json.loads(resp[0]), status=resp[1])
