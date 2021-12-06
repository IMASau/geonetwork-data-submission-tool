# TODO: Suspect this could go into tern.api app
# TODO: Suspect this could go into metcalf.api app (common stuff)

import django_filters
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import filters, renderers, response, viewsets
from rest_framework.decorators import api_view
from rest_framework.generics import get_object_or_404

from metcalf.common import xmlutils4
from metcalf.tern.backend import models
from metcalf.tern.backend import serializers
from metcalf.tern.frontend.filters import ParentFilter


class DumaDocumentViewSet(viewsets.ReadOnlyModelViewSet):
    lookup_field = 'uuid'
    renderer_classes = [renderers.JSONRenderer]

    def get_queryset(self):
        queryset = models.Document.objects.filter(hasUserDefined=True)
        submitted = self.request.query_params.get('submitted', 'true')
        if submitted.lower() != 'false':
            queryset = queryset.filter(status=models.Document.SUBMITTED)
        return queryset

    def get_serializer_class(self):
        if 'uuid' in self.kwargs:
            return serializers.DumaTermListSerializer
        return serializers.DumaDocumentSerializer


@api_view(['PUT'])
def duma_update(request, *args, **kwargs):
    docid = kwargs.get('docid')
    termid = kwargs.get('termid')
    data = request.data

    if 'duma_path' not in data:
        raise Exception('Missing expected property "duma_path"')
    path = data['duma_path'].lstrip('.').split('.')

    document = get_object_or_404(models.Document, uuid=docid)
    document_draft = document.latest_draft
    document_data = document_draft.data

    updated_doc = xmlutils4.update_user_defined(document_data, data, path)

    newdraft = models.DraftMetadata.objects.create(document=document, user=request.user, data=updated_doc)
    newdraft.noteForDataManager = document_draft.noteForDataManager
    newdraft.agreedToTerms = document_draft.agreedToTerms
    newdraft.doiRequested = document_draft.doiRequested
    newdraft.save()

    document.hasUserDefined = bool(xmlutils4.extract_user_defined(updated_doc))
    document.save()

    return response.Response(updated_doc)


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
