# TODO: move to tern.api app?

from rest_framework import serializers

from metcalf.imas.backend import models


class InstitutionSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.Institution
        fields = [
            # 'id',
            'uri',
            'prefLabel',
            # 'altLabel',
            # 'exactMatch',
            # 'isUserAdded',
            'organisationName',
            'deliveryPoint',
            'deliveryPoint2',
            'city',
            'administrativeArea',
            'postalCode',
            'country',
        ]


class PersonSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.Person
        fields = '__all__'


class SamplingFrequencySerializer(serializers.ModelSerializer):
    class Meta:
        model = models.SamplingFrequency
        fields = '__all__'


class HorizontalResolutionSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.HorizontalResolution
        fields = '__all__'


class TopicCategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = models.TopicCategory
        fields = '__all__'


class MetadataTemplateSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.MetadataTemplate
        fields = ('id', 'name',)


class ScienceKeywordSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.ScienceKeyword
        fields = '__all__'


class AnzsrcKeywordSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.AnzsrcKeyword
        fields = '__all__'


class RoleCodeSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.RoleCode
        fields = '__all__'


class BaseParameterSerializer(serializers.ModelSerializer):
    children_count = serializers.SerializerMethodField()

    def get_children_count(self, obj):
        return obj.get_children_count()


class ParameterNameSerializer(BaseParameterSerializer):
    class Meta:
        model = models.ParameterName
        fields = [
            # "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
            "Name", "URI",
            # "Version", "Definition",
            # "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
        ]


class ParameterUnitSerializer(BaseParameterSerializer):
    class Meta:
        model = models.ParameterUnit
        fields = [
            # "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
            "Name", "URI",
            # "Version", "Definition",
            # "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
        ]


class ParameterInstrumentSerializer(BaseParameterSerializer):
    class Meta:
        model = models.ParameterInstrument
        fields = [
            # "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
            "Name", "URI",
            # "Version", "Definition",
            # "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
        ]


class ParameterPlatformSerializer(BaseParameterSerializer):
    class Meta:
        model = models.ParameterPlatform
        fields = [
            # "id", "children_count", "lft", "rgt", "tree_id", "depth", "is_selectable",
            "Name", "URI",
            # "Version", "Definition",
            # "term", "vocabularyTermURL", "vocabularyVersion", "termDefinition",
        ]
