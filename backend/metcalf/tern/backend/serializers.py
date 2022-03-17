# TODO: move to tern.api app?

from rest_framework import serializers
from rest_framework.reverse import reverse

from metcalf.common import xmlutils4
from metcalf.tern.backend import models


class DocumentCategoryListSerializer(serializers.ListSerializer):
    def update(self, instance, validated_data):
        doc_mapping = {d.id: d for d in instance}
        data_mapping = {item['id']: item for item in validated_data}

        # Creations / updates using primary key; ignore deletions
        ret = []
        for doc_id, data in data_mapping.items():
            doc = doc_mapping.get(doc_id, None)
            if doc is None:
                ret.append(self.child.create(data))
            else:
                ret.append(self.child.update(doc, data))
        return ret


class DocumentCategorySerializer(serializers.ModelSerializer):
    # Make writeable so we can access it (need to preserve from input data):
    id = serializers.IntegerField()

    class Meta:
        model = models.DocumentCategory
        fields = ('id', 'name', 'label')
        list_serializer_class = DocumentCategoryListSerializer


class DumaDocumentSerializer(serializers.ModelSerializer):
    # The defaul returns the document-info url, we want the duma-specific one
    url = serializers.SerializerMethodField()

    def get_url(self, obj):
        model_id = obj.uuid
        request = self.context['request']
        # HACK: if document isn't submitted, add flag:
        detail_url = reverse('duma-detail', args=[model_id], request=request)
        is_submitted = obj.status == models.Document.SUBMITTED
        return detail_url if is_submitted else detail_url + '?submitted=false'

    class Meta:
        model = models.Document
        fields = ('title', 'uuid', 'url',)


class DumaTermListSerializer(serializers.BaseSerializer):
    def to_representation(self, instance):
        # May need to normalise terms
        # label, description, uri, duma_path, url
        request = self.context['request']
        update_url = reverse('duma-detail', args=[instance.uuid], request=request)
        # HACK: if document isn't submitted, add flag:
        is_submitted = instance.status == models.Document.SUBMITTED
        update_url = update_url if is_submitted else update_url + '?submitted=false'
        user_defined = xmlutils4.extract_user_defined(instance.latest_draft.data)
        return [dict(d, url=update_url) for d in user_defined]


# Used for deserializing input:
# class DumaTermSerializer(serializers.Serializer):
#     label = serializers.CharField(max_length=200)
#     description = serializers.CharField(max_length=200)
#     uri = serializers.UUIDField()
#     isUserDefined = serializers.BooleanField()
#     duma_path = serializers.CharField(max_length=200)


# class InstitutionSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.Institution
#         fields = '__all__'


# class PersonSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.Person
#         fields = '__all__'


# class SamplingFrequencySerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.SamplingFrequency
#         fields = '__all__'


# class HorizontalResolutionSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.HorizontalResolution
#         fields = '__all__'


# class TopicCategorySerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.TopicCategory
#         fields = '__all__'


class MetadataTemplateSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.MetadataTemplate
        fields = ('id', 'name',)

# class ScienceKeywordSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.ScienceKeyword
#         fields = '__all__'


# class AnzsrcKeywordSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.AnzsrcKeyword
#         fields = '__all__'


# class RoleCodeSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = models.RoleCode
#         fields = '__all__'


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# class BaseParameterSerializer(serializers.ModelSerializer):
#     children_count = serializers.SerializerMethodField()
#
#     def get_children_count(self, obj):
#         return obj.get_children_count()


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# class ParameterNameSerializer(BaseParameterSerializer):
#     class Meta:
#         model = models.ParameterName
#         fields = [
#             "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
#             "Name", "URI", "Version", "Definition",
#             "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
#         ]


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# class ParameterUnitSerializer(BaseParameterSerializer):
#     class Meta:
#         model = models.ParameterUnit
#         fields = [
#             "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
#             "Name", "URI", "Version", "Definition",
#             "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
#         ]


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# class ParameterInstrumentSerializer(BaseParameterSerializer):
#     class Meta:
#         model = models.ParameterInstrument
#         fields = [
#             "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
#             "Name", "URI", "Version", "Definition",
#             "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
#         ]


# TODO: Remove. Not used anymore since we pull from Elasticsearch.
# class ParameterPlatformSerializer(BaseParameterSerializer):
#     class Meta:
#         model = models.ParameterPlatform
#         fields = [
#             "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
#             "Name", "URI", "Version", "Definition",
#             "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
#         ]
