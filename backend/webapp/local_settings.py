from .settings import *

STATIC_ROOT = os.path.join(BASE_DIR, "static")
MEDIA_ROOT = os.path.join(BASE_DIR, "media")

STATICFILES_DIRS = (
    ('metcalf3', os.path.join(BASE_DIR, "../frontend/resources/public")),
)
