#!/bin/sh

mkdir -p /var/www/metcalf/shared/staticfiles/metcalf3/
cp -R /var/www/metcalf/frontend/resources/public/* /var/www/metcalf/shared/staticfiles/metcalf3/

exec nginx -g 'daemon off;'