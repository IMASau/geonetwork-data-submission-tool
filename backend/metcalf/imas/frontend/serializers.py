from django.contrib.auth.models import User
from django.urls import reverse
from rest_framework import serializers

from metcalf.common.serializers import AbstractDocumentInfoSerializer
from metcalf.imas.backend.models import Document, DocumentAttachment, MetadataTemplate
from metcalf.imas.frontend.models import SiteContent, DataSource


class DocumentInfoSerializer(AbstractDocumentInfoSerializer):
    class Meta:
        model = Document
        fields = ('uuid', 'title', 'owner', 'contributors', 'last_updated', 'last_updated_by',
                  'url', 'clone_url', 'transition_url', 'export_url',
                  'status', 'transitions', 'is_editor', 'is_contributor')


class AttachmentSerializer(serializers.ModelSerializer):
    delete_url = serializers.SerializerMethodField()
    name = serializers.SerializerMethodField()

    class Meta:
        model = DocumentAttachment
        fields = ('id', 'name', 'file', 'created', 'modified', 'delete_url')

    def get_name(self, inst):
        return inst.name

    def get_delete_url(self, inst):
        return reverse("DeleteAttachment", kwargs={'uuid': inst.document.uuid, 'id': inst.id})


class SiteContentSerializer(serializers.ModelSerializer):
    class Meta:
        model = SiteContent
        fields = ('title', 'organisation_url', 'email', 'tag_line', 'guide_pdf', 'portal_title', 'portal_url')


class CreateDocumentTemplateSerializer(serializers.ModelSerializer):
    class Meta:
        model = MetadataTemplate
        fields = ['id', 'name']


class CreateDocumentSerializer(serializers.ModelSerializer):
    template = CreateDocumentTemplateSerializer()

    class Meta:
        model = Document
        fields = ['title', 'template']


class DataSourceSerializer(serializers.ModelSerializer):
    schema = serializers.SerializerMethodField()

    class Meta:
        model = DataSource
        fields = (
            'slug',
            'base_url',
            'search_param',
            'response_key',
            'schema',
        )

    def get_schema(self, inst):
        return inst.schema


class DocUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('email',)


class DocumentSerializer(serializers.ModelSerializer):
    owner = DocUserSerializer(read_only=True)
    contributors = DocUserSerializer(read_only=True, many=True)

    class Meta:
        model = Document
        fields = ('uuid', 'title', 'status', 'owner', 'contributors',)
