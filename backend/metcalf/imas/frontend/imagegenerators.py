from imagekit import ImageSpec, register
from imagekit.processors import ResizeToFill


class Thumbnail(ImageSpec):
    processors = [ResizeToFill(2048, 1024)]
    format = 'JPEG'
    options = {'quality': 60}


register.generator('imas:frontend:homepage_image', Thumbnail)
