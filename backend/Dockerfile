# start from an official image
FROM python:3-alpine

ENV DEBIAN_FRONTEND noninteractive

# arbitrary location choice: you can change the directory
RUN mkdir -p /var/www/metcalf
WORKDIR /var/www/metcalf

# set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# install our dependencies
# we use --system flag because we don't need an extra virtualenv
COPY prod_requirements.txt /var/www/metcalf/
COPY requirements.txt /var/www/metcalf/
RUN apk update && apk add python3-dev  libxml2-dev libxslt-dev  musl-dev libffi-dev zlib  libjpeg
RUN apk update && apk add postgresql-dev gcc 
RUN apk add jpeg-dev zlib-dev freetype-dev lcms2-dev openjpeg-dev tiff-dev tk-dev tcl-dev
RUN pip install -r prod_requirements.txt --upgrade
RUN pip install gunicorn
RUN apk add ca-certificates

# copy our project code
COPY . /var/www/metcalf/backend

COPY sys.conf /etc/sysctl.d/99-sysctl.conf
COPY application_dir.j2 /etc/tmpfiles.d/gunicorn_metcalf.conf
COPY application_socket.j2 /etc/systemd/system/gunicorn_metcalf.socket
COPY application_systemd.j2 /etc/systemd/system/gunicorn_metcalf.service

COPY usermetcalf /etc/sudoers.d/metcalf
RUN chown -R root:root /etc/sudoers.d/metcalf
RUN chmod 0440 /etc/sudoers.d/metcalf

COPY docker.cron /etc/cron.d/metcalf-cron
RUN chmod 0644 /etc/cron.d/metcalf-cron
RUN crontab /etc/cron.d/metcalf-cron
RUN touch /var/log/cron.log
RUN chmod 744 /var/log/cron.log 

# define the default command to run when starting the container
ENV DEBIAN_FRONTEND teletype

COPY ./entrypoint.sh /var/www/metcalf/backend/entrypoint.sh
RUN chmod 0777 /var/www/metcalf/backend/entrypoint.sh

# run entrypoint.sh
ENTRYPOINT ["/var/www/metcalf/backend/entrypoint.sh"]