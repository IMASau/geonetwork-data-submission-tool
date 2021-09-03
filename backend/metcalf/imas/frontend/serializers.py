from django.contrib.auth.models import User
from django.urls import reverse
from rest_framework import serializers

from metcalf.imas.backend.models import Document, DocumentAttachment
from metcalf.imas.frontend.models import SiteContent


class UserSerializer(serializers.ModelSerializer):
    groups = serializers.StringRelatedField(many=True)
    permissions = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name', 'groups', 'permissions', 'is_superuser', 'is_staff')

    def get_permissions(self, obj):
        permissions = set()
        for p in obj.user_permissions.all():
            permissions.add(p)
        for g in obj.groups.all():
            for p in g.permissions.all():
                permissions.add(p)
        # TODO: might need applabel.codename format
        return ["{0}.{1}".format(p.content_type.app_label, p.codename) for p in permissions]


class UserInfoSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name')


class DocumentInfoSerializer(serializers.ModelSerializer):
    owner = UserInfoSerializer()
    url = serializers.SerializerMethodField()
    clone_url = serializers.SerializerMethodField()
    transition_url = serializers.SerializerMethodField()
    export_url = serializers.SerializerMethodField()
    last_updated = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    transitions = serializers.SerializerMethodField()

    class Meta:
        model = Document
        fields = ('uuid', 'title', 'owner', 'last_updated',
                  'url', 'clone_url', 'transition_url', 'export_url',
                  'status', 'transitions')

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

    def get_status(self, doc):
        return doc.get_status_display()

    def get_transitions(self, doc):
        return [t.method.__name__
                for t in doc.get_available_user_status_transitions(self.context['user'])]


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
                  'roadmap_pdf', 'releasenotes_url',
                  'portal_title', 'portal_url')
