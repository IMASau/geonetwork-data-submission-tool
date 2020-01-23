#!/bin/sh

if [ "$1" == "init" ] ; then
  python3 manage.py migrate --noinput
  python3 manage.py setup_superuser --noinput
  python3 manage.py loaddata setup.json
  python3 manage.py collectstatic --noinput
  # copy xml_templates to media folder if newer than existing files
  cp -u -v -r /data-submission-tool/xml_template/* /data/media/
  shift
fi

# don't leak certain env vars
unset DJANGO_SUPERUSER_PASSWORD
unset DJANGO_SUPERUSER_USERNAME
unset DJANGO_SUPERUSER_EMAIL

exec "$@"