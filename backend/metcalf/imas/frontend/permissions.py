from rest_framework.exceptions import PermissionDenied


def is_document_editor(request, doc):
    if not doc.is_editor(request.user):
        raise PermissionDenied()


def is_document_contributor(request, doc):
    if not (doc.is_editor(request.user) or doc.is_contributor(request.user)):
        raise PermissionDenied()
