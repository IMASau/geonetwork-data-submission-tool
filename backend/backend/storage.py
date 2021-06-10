from django.urls import reverse
from django.utils.encoding import filepath_to_uri
from swift.storage import SwiftStorage


class AttachmentStorage(SwiftStorage):

    def url(self, name):
        # this depends an view 'DownloadAttachment' being set up
        url = reverse('DownloadAttachment', kwargs={'path': filepath_to_uri(name)})
        # TODO: relative url's may be nice?
        #       at least to store in metadata json?
        return url


attachment_store = AttachmentStorage()


# prepend document.uuid to file path
def document_upload_path(instance, filename):
    return "/".join((
        str(instance.document.uuid),
        filename.replace(" ", "_"),
    ))
