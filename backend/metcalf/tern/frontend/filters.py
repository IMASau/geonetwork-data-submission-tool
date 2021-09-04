from django.core.exceptions import ObjectDoesNotExist
from rest_framework import filters


class ParentFilter(filters.BaseFilterBackend):

    def filter_queryset(self, request, queryset, view):
        parent = request.query_params.get('parent', None)
        if parent is not None:
            try:
                queryset = queryset.get(pk=parent).get_children()
            except ObjectDoesNotExist:
                queryset = queryset.none()
        return queryset

    def get_fields(self, view):
        return ['parent']
