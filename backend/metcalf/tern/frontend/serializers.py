from django.contrib.auth.models import User
from django.urls import reverse
from rest_framework import serializers

from metcalf.common.serializers import UserInfoSerializer
from metcalf.tern.backend.models import Document, DocumentAttachment, MetadataTemplate
from metcalf.tern.frontend.models import SiteContent


class DocumentInfoSerializer(serializers.ModelSerializer):
    owner = UserInfoSerializer()
    contributors = UserInfoSerializer(many=True)
    url = serializers.SerializerMethodField()
    clone_url = serializers.SerializerMethodField()
    transition_url = serializers.SerializerMethodField()
    export_url = serializers.SerializerMethodField()
    last_updated = serializers.SerializerMethodField()
    last_updated_by = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    transitions = serializers.SerializerMethodField()
    is_editor = serializers.SerializerMethodField()
    is_contributor = serializers.SerializerMethodField()

    class Meta:
        model = Document
        fields = ('uuid', 'title', 'owner', 'contributors', 'last_updated', 'last_updated_by',
                  'url', 'clone_url', 'transition_url', 'export_url',
                  'status', 'transitions', 'is_editor', 'is_contributor')

    def get_url(self, doc):
        return reverse("Edit", kwargs={'uuid': doc.uuid})

    def get_transition_url(self, doc):
        return reverse("Transition", kwargs={'uuid': doc.uuid})

    def get_clone_url(self, doc):
        return reverse("Clone", kwargs={'uuid': doc.uuid})

    def get_export_url(self, doc):
        return reverse("Export", kwargs={'uuid': doc.uuid})

    def get_last_updated(self, doc):
        drafts = doc.draftmetadata_set
        if drafts.exists():
            return drafts.all()[0].time

    def get_last_updated_by(self, doc):
        drafts = doc.draftmetadata_set
        if drafts.exists():
            return UserInfoSerializer(instance=drafts.all()[0].user).data

    def get_status(self, doc):
        return doc.get_status_display()

    def get_transitions(self, doc):
        return [t.method.__name__
                for t in doc.get_available_user_status_transitions(self.context['user'])]

    def get_is_editor(self, doc):
        return doc.is_editor(self.context['user'])

    def get_is_contributor(self, doc):
        return doc.is_contributor(self.context['user'])


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
