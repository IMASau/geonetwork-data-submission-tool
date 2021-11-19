from django.contrib.auth.models import User
from django.core.exceptions import ObjectDoesNotExist, MultipleObjectsReturned
from rest_framework import serializers


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
