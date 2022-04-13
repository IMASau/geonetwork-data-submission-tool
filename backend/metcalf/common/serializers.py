from django.contrib.auth.models import User
from django.core.exceptions import ObjectDoesNotExist, MultipleObjectsReturned
from django.urls import reverse
from rest_framework import serializers

from metcalf.common.models import AbstractDocument


class UserInfoSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name')


class UserByEmailSerializer(serializers.Serializer):
    email = serializers.EmailField()

    def validate_email(self, email):
        try:
            User.objects.get(email=email)
        except ObjectDoesNotExist:
            raise serializers.ValidationError("No matching users")
        except MultipleObjectsReturned:
            raise serializers.ValidationError("Multiple matching users")

        return email


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
        return ["{0}.{1}".format(p.content_type.app_label, p.codename) for p in permissions]


class AbstractDocumentInfoSerializer(serializers.ModelSerializer):
    owner = UserInfoSerializer()
    contributors = UserInfoSerializer(many=True)
    url = serializers.SerializerMethodField()
    clone_url = serializers.SerializerMethodField()
    transition_url = serializers.SerializerMethodField()
    export_url = serializers.SerializerMethodField()
    share_url = serializers.SerializerMethodField()
    last_updated = serializers.SerializerMethodField()
    last_updated_by = serializers.SerializerMethodField()
    status = serializers.SerializerMethodField()
    transitions = serializers.SerializerMethodField()
    is_editor = serializers.SerializerMethodField()
    is_contributor = serializers.SerializerMethodField()

    class Meta:
        model = AbstractDocument
        fields = ('uuid', 'title', 'owner', 'contributors', 'last_updated', 'last_updated_by',
                  'url', 'clone_url', 'transition_url', 'export_url', 'share_url',
                  'status', 'transitions', 'is_editor', 'is_contributor')
        abstract = True

    def get_url(self, doc):
        return reverse("Edit", kwargs={'uuid': doc.uuid})

    def get_transition_url(self, doc):
        return reverse("Transition", kwargs={'uuid': doc.uuid})

    def get_clone_url(self, doc):
        return reverse("Clone", kwargs={'uuid': doc.uuid})

    def get_export_url(self, doc):
        return reverse("Export", kwargs={'uuid': doc.uuid})

    def get_share_url(self, doc):
        return reverse("share", kwargs={'uuid': doc.uuid})

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
