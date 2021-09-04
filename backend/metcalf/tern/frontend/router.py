from django.db.models import CharField, Model, Manager, QuerySet
from rest_framework import serializers, viewsets, filters, permissions


def add_basic_route(MyModel, router):
    """
    Quick and dirty API helper.  Generates route for model with standard options.
    """

    class MyModelSerializer(serializers.ModelSerializer):
        class Meta:
            model = MyModel

    MyModel._meta._rest_serializer = MyModelSerializer

    class MyViewSet(viewsets.ModelViewSet):
        permission_class = (permissions.DjangoModelPermissionsOrAnonReadOnly,)
        queryset = MyModel.objects.all()
        serializer_class = MyModelSerializer
        filter_backends = (filters.DjangoFilterBackend, filters.OrderingFilter, filters.SearchFilter)
        filter_fields = MyModel._meta.get_all_field_names()
        ordering_fields = MyModel._meta.get_all_field_names()
        search_fields = [f.attname for f in MyModel._meta.concrete_fields if isinstance(f, CharField)]

    router.register(MyModel._meta.app_label + "/" + MyModel._meta.model_name, MyViewSet)


def get_rest_serializer(x):
    if (isinstance(x, QuerySet)):
        return x.model._meta._rest_serializer
    elif (isinstance(x, Manager)):
        return x.model._meta._rest_serializer
    elif (isinstance(x, Model)):
        return type(x)._meta._rest_serializer
    else:
        assert "Unexpected type " + type(x)


def rest_serialize(x, *args, **kwargs):
    """
    Quick and dirty REST serialization.
    """
    serializer = get_rest_serializer(x)

    assert serializer, "Unable to resolve a serializer for " + type(x)

    if (isinstance(x, QuerySet) or isinstance(x, Manager)):
        # If unset, choose many for querysets and managers
        kwargs['many'] = kwargs.get('many', True)

    return serializer(x, *args, **kwargs)
