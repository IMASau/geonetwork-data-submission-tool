from django.urls import reverse
from rest_framework import serializers

from metcalf.common.serializers import AbstractDocumentInfoSerializer
from metcalf.tern.backend.models import Document, DocumentAttachment, MetadataTemplate
from metcalf.tern.frontend.models import SiteContent


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
        fields = ('title', 'organisation_url', 'email', 'tag_line', 'guide_pdf', 'terms_pdf',
                  'roadmap_pdf', 'releasenotes_url', 'portal_title', 'portal_url')


class CreateDocumentTemplateSerializer(serializers.ModelSerializer):
    class Meta:
        model = MetadataTemplate
        fields = ['id', 'name']


class CreateDocumentSerializer(serializers.ModelSerializer):
    template = CreateDocumentTemplateSerializer()

    class Meta:
        model = Document
        fields = ['title', 'template']
