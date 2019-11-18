#!/bin/sh

if [ "$DATABASE" = "postgres" ]
then
    echo "Waiting for postgres..."

    while ! nc -z $SQL_HOST $SQL_PORT; do
      sleep 0.1
    done

    echo "PostgreSQL started"
fi

#python manage.py flush --no-input
python manage.py collectstatic --noinput
python manage.py migrate --no-input || true
python manage.py loaddata setup.json || true

pwd

echo "from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.create_superuser('tern', 'esupport@tern.org.au', 'change-me')" | python manage.py shell || true
mkdir -p /var/www/metcalf/shared/media/images
cp /var/www/metcalf/backend/buildfiles/Hemispherical-Photography.jpg /var/www/metcalf/shared/media/images/
mkdir -p /var/www/metcalf/shared/media/guide
cp /var/www/metcalf/backend/buildfiles/Coming_Soon_Help_KrszZhh.pdf /var/www/metcalf/shared/media/guide/
cp /var/www/metcalf/backend/buildfiles/Coming_Soon_Roadmap.pdf /var/www/metcalf/shared/media/guide/
mkdir -p /var/www/metcalf/shared/media/terms
cp /var/www/metcalf/backend/buildfiles/TERN_Data_Provider_Deed_v1_9_DST.pdf /var/www/metcalf/shared/media/terms/
cp /var/www/metcalf/backend/buildfiles/Metadata_Template.xml /var/www/metcalf/shared/media/
cp /var/www/metcalf/backend/buildfiles/tern_template_spec.json /var/www/metcalf/shared/media/

touch /var/log/cron.log
chmod 744 /var/log/cron.log 

sleep 1
rm -rf /var/www/metcalf/backend/buildfiles/

exec "$@"