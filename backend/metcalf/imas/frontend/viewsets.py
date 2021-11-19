from django.db.models import Q
from rest_framework import viewsets

from metcalf.imas.backend import models
from metcalf.imas.frontend import serializers


class DocumentViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = models.Document.objects.all()
    serializer_class = serializers.DocumentSerializer

    def get_queryset(self):
        return (self.queryset
                .filter(Q(owner=self.request.user) | Q(contributors=self.request.user))
                .exclude(status=models.Document.DISCARDED))
