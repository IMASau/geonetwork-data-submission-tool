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
