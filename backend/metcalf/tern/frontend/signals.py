import os
from django.core.files.storage import default_storage
from django.dispatch import receiver
from django.shortcuts import get_object_or_404
from django_tus.signals import tus_upload_finished_signal

from metcalf.tern.backend.models import Document, DocumentAttachment

@receiver(tus_upload_finished_signal)
def create_file(sender, **kwargs):
    document = get_object_or_404(Document, uuid=kwargs['metadata']['document'])

    DocumentAttachment.objects.create(
        document = document,
        name = kwargs['metadata']['name'],
        file = kwargs['filename'],
        resourceId = os.path.basename(kwargs['upload_file_path'])
    )
