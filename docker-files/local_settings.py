from .settings import *

ADMINS=[("Wilma", "w.karsdrop@uq.edu.au")]

STATIC_ROOT = '/data/static/'
STATICFILES_DIRS = (
    ("metcalf3", '/data-submission-tool/frontend/resources/public'),
    # os.path.join(BASE_DIR, 'webapp/static'),
)

MEDIA_ROOT = '/data/media/'
