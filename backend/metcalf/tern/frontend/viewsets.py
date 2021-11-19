from django.db.models import Q
from rest_framework import viewsets

from metcalf.tern.backend import models
from metcalf.tern.frontend import serializers


class DocumentInfoViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = models.Document.objects.all()
    serializer_class = serializers.DocumentInfoSerializer

    def get_serializer_context(self):
        context = super(DocumentInfoViewSet, self).get_serializer_context()
        context.update({"user": self.request.user})
        return context

    def get_queryset(self):
        return (self.queryset
                .filter(Q(owner=self.request.user) | Q(contributors=self.request.user))
                .exclude(status=models.Document.DISCARDED))
