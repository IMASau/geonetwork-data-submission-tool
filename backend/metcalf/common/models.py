from django.db import models


def no_spaces_in_filename(instance, filename):
    return filename.replace(" ", "_")


class AbstractDocumentAttachment(models.Model):
    document = models.ForeignKey("Document", on_delete=models.CASCADE, related_name='attachments')
    name = models.CharField(max_length=256)
    file = models.FileField(upload_to=no_spaces_in_filename)
    created = models.DateTimeField(auto_now_add=True)
    modified = models.DateTimeField(auto_now=True)

    class Meta:
        abstract = True
