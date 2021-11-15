from .settings import *

# HERE STARTS DYNACONF EXTENSION LOAD (Keep at the very bottom of settings.py)
# Read more at https://dynaconf.readthedocs.io/en/latest/guides/django.html
import dynaconf  # noqa

settings = dynaconf.DjangoDynaconf(
    __name__,
    # ENVVAR_PREFIX_FOR_DYNACONF='DST',
    ENVVAR_FOR_DYNACONF="DST_SETTINGS",
    validators=[dynaconf.Validator("ELASTICSEARCH_VERIFY_SSL", is_type_of=bool)],
)  # noqa
# HERE ENDS DYNACONF EXTENSION LOAD (No more code below this line)

