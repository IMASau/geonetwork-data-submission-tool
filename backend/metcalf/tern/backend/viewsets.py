# TODO: Suspect this could go into tern.api app
# TODO: Suspect this could go into metcalf.api app (common stuff)

import django_filters
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import filters, renderers, viewsets
from rest_framework.generics import get_object_or_404

from metcalf.tern.backend import models
from metcalf.tern.backend import serializers
from metcalf.tern.frontend.filters import ParentFilter


# TODO (desired structure):
# * GET /api/duma: list of URLs (of docs containing user-added terms) {title:'asdf', url:"/api/duma/...."}
# * GET /api/duma/<docid>: list of URLs (list of user-added terms including URL)
# * POST /api/duma/<docid>/<termid>:
# * (alternate) POST /api/duma/<termid>: ie, URL is basically opaque, and comes from the instance
# NOTE: we do need the document to look up where to insert the term!  So can't ditch that
# * get endpoint (/api/duma/<doc-uuid>) returns a list of user-added terms
# * a user-added term also has a URL (/api/duma/<doc-uuid>/<term-uuid>)
# * Can POST to a term endpoint to update it

# May want to create an even more stripped back version:
class DumaDocumentViewSet(viewsets.ReadOnlyModelViewSet):
    lookup_field = 'uuid'
    queryset = models.Document.objects.filter(hasUserDefined=True)  # FIXME: and status=SUBMITTED
    serializer_class = serializers.DumaDocumentSerializer
    renderer_classes = [renderers.JSONRenderer]

    def get_serializer_class(self):
        if 'uuid' in self.kwargs:
            return serializers.DumaTermListSerializer
        return serializers.DumaDocumentSerializer


class DumaTermViewSet(viewsets.GenericViewSet):
    pass


# FIXME: how about a constant model for name/value/uuid?

# FIXME: the POST endpoint probably should:
# * check the UUID at that path
# * If it doesn't match, do we abort, or try harder (exhaustive search for that uuid?)
# * only update the uri
# * serializer for the individual components can extract those components (?)


class InstitutionViewSet(viewsets.ModelViewSet):
    queryset = models.Institution.objects.all().order_by('prefLabel')
    serializer_class = serializers.InstitutionSerializer

    # search_fields = ('organisationName', 'deliveryPoint', 'deliveryPoint2',
    #                 'city', 'administrativeArea', 'postalCode', 'country')


class PersonViewSet(viewsets.ModelViewSet):
    serializer_class = serializers.PersonSerializer
    search_fields = ('familyName', 'givenName', 'prefLabel', 'electronicMailAddress')
    queryset = models.Person.objects.all()

    def get_queryset(self):
        queryset = models.Person.objects.all()
        uri = self.request.query_params.get('uri', None)
        if uri is not None:
            queryset = queryset.filter(uri=uri)
        return queryset


class SamplingFrequencyViewSet(viewsets.ModelViewSet):
    queryset = models.SamplingFrequency.objects.all().order_by('prefLabelSortText')
    serializer_class = serializers.SamplingFrequencySerializer
    search_fields = ('prefLabel')


class HorizontalResolutionViewSet(viewsets.ModelViewSet):
    queryset = models.HorizontalResolution.objects.all().order_by('prefLabelSortText')
    serializer_class = serializers.HorizontalResolutionSerializer
    search_fields = ('prefLabel')


class TopicCategoryViewSet(viewsets.ModelViewSet):
    queryset = models.TopicCategory.objects.all()
    serializer_class = serializers.TopicCategorySerializer
    search_fields = ('identifier')


class MetadataTemplateViewSet(viewsets.ModelViewSet):
    queryset = models.MetadataTemplate.objects.all()
    serializer_class = serializers.MetadataTemplateSerializer
    search_fields = ('name',)


class ScienceKeywordViewSet(viewsets.ModelViewSet):
    queryset = models.ScienceKeyword.objects.all()
    serializer_class = serializers.ScienceKeywordSerializer
    search_fields = ('Category', 'Topic', 'Term', 'VariableLevel1',
                     'VariableLevel2', 'VariableLevel3', 'DetailedVariable')


class AnzsrcKeywordViewSet(viewsets.ModelViewSet):
    queryset = models.AnzsrcKeyword.objects.all()
    serializer_class = serializers.AnzsrcKeywordSerializer

    search_fields = ('Category', 'Topic', 'Term', 'VariableLevel1',
                     'VariableLevel2', 'VariableLevel3', 'DetailedVariable')


class RoleCodeViewSet(viewsets.ModelViewSet):
    queryset = models.RoleCode.objects.all()
    serializer_class = serializers.RoleCodeSerializer

    search_fields = ('Identifier', 'Description')


# TODO: Remove. Not used anymore since we pull TERN parameters from Elasticsearch.
class ParameterNameNodeFilter(django_filters.FilterSet):
    min_lft = django_filters.NumberFilter(field_name="lft", lookup_expr='gte')
    max_rgt = django_filters.NumberFilter(field_name="rgt", lookup_expr='lte')

    class Meta:
        model = models.ParameterName
        fields = ['depth', 'tree_id', 'min_lft', 'max_rgt']


# TODO: Remove. Not used anymore since we pull TERN parameters from Elasticsearch.
class ParameterNameViewSet(viewsets.ModelViewSet):
    queryset = models.ParameterName.objects.all().order_by('Name')
    serializer_class = serializers.ParameterNameSerializer
    filter_backends = (filters.SearchFilter, DjangoFilterBackend, ParentFilter)
    filter_class = ParameterNameNodeFilter
    search_fields = ('Name', 'Definition')


# TODO: Remove. Not used anymore since we pull TERN parameters from Elasticsearch.
class ParameterUnitNodeFilter(django_filters.FilterSet):
    min_lft = django_filters.NumberFilter(field_name="lft", lookup_expr='gte')
    max_rgt = django_filters.NumberFilter(field_name="rgt", lookup_expr='lte')
    min_depth = django_filters.NumberFilter(field_name="depth", lookup_expr='gte')
    max_depth = django_filters.NumberFilter(field_name="depth", lookup_expr='lte')

    class Meta:
        model = models.ParameterUnit
        fields = ['depth', 'tree_id', 'min_lft', 'max_rgt', 'min_depth', 'max_depth']


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
class ParameterUnitViewSet(viewsets.ModelViewSet):
    queryset = models.ParameterUnit.objects.all().order_by('Name')
    serializer_class = serializers.ParameterUnitSerializer
    filter_backends = (filters.SearchFilter, DjangoFilterBackend, filters.OrderingFilter, ParentFilter)
    filter_class = ParameterUnitNodeFilter
    search_fields = ('Name', 'Definition')
    ordering_fields = ('tree_id', 'Name')


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
class ParameterInstrumentNodeFilter(django_filters.FilterSet):
    min_lft = django_filters.NumberFilter(field_name="lft", lookup_expr='gte')
    max_rgt = django_filters.NumberFilter(field_name="rgt", lookup_expr='lte')

    class Meta:
        model = models.ParameterInstrument
        fields = ['depth', 'tree_id', 'min_lft', 'max_rgt']


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
class ParameterInstrumentViewSet(viewsets.ModelViewSet):
    queryset = models.ParameterInstrument.objects.all().order_by('Name')
    serializer_class = serializers.ParameterInstrumentSerializer
    filter_backends = (filters.SearchFilter, DjangoFilterBackend, ParentFilter)
    filter_class = ParameterInstrumentNodeFilter
    search_fields = ('Name', 'Definition')


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
class ParameterPlatformNodeFilter(django_filters.FilterSet):
    min_lft = django_filters.NumberFilter(field_name="lft", lookup_expr='gte')
    max_rgt = django_filters.NumberFilter(field_name="rgt", lookup_expr='lte')

    class Meta:
        model = models.ParameterPlatform
        fields = ['depth', 'tree_id', 'min_lft', 'max_rgt']


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
class ParameterPlatformViewSet(viewsets.ModelViewSet):
    queryset = models.ParameterPlatform.objects.all().order_by('Name')
    serializer_class = serializers.ParameterPlatformSerializer
    filter_backends = (filters.SearchFilter, DjangoFilterBackend, ParentFilter)
    filter_class = ParameterPlatformNodeFilter
    ordering_fields = ['Name']
    search_fields = ('Name', 'Definition')
