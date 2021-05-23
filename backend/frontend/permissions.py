from rest_framework.exceptions import PermissionDenied


def is_document_editor(request, doc):
    if not doc.is_editor(request.user):
        raise PermissionDenied()
