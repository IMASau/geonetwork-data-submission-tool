# start from an official image
FROM python:3-alpine

ENV DEBIAN_FRONTEND noninteractive

# arbitrary location choice: you can change the directory
RUN mkdir -p /var/www/metcalf/backend/
WORKDIR /var/www/metcalf

# set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

RUN apk update \
    && apk add ca-certificates \
    freetype-dev \
    gcc \
    jpeg-dev \
    lcms2-dev \
    libxml2-dev \
    libxslt-dev \
    libffi-dev \
    libjpeg \
    musl-dev \
    postgresql-dev \
    openjpeg-dev \
    python3-dev \
    tiff-dev \
    tk-dev \
    tcl-dev \
    zlib \
    zlib-dev

COPY buildfiles/prod_requirements.txt /var/www/metcalf/
COPY buildfiles/requirements.txt /var/www/metcalf/
RUN pip install -r prod_requirements.txt --upgrade
RUN pip install gunicorn

COPY buildfiles/sys.conf /etc/sysctl.d/99-sysctl.conf
COPY buildfiles/application_dir.j2 /etc/tmpfiles.d/gunicorn_metcalf.conf
COPY buildfiles/application_socket.j2 /etc/systemd/system/gunicorn_metcalf.socket
COPY buildfiles/application_systemd.j2 /etc/systemd/system/gunicorn_metcalf.service
COPY buildfiles/usermetcalf /etc/sudoers.d/metcalf
#COPY buildfiles/docker.cron /etc/cron.d/metcalf-cron
COPY buildfiles/entrypoint.sh /var/www/metcalf/backend/entrypoint.sh

RUN chown -R root:root /etc/sudoers.d/metcalf
RUN chmod 0440 /etc/sudoers.d/metcalf

#RUN chmod 0644 /etc/cron.d/metcalf-cron
#RUN crontab /etc/cron.d/metcalf-cron

# copy our project code
COPY . /var/www/metcalf/backend

# define the default command to run when starting the container
ENV DEBIAN_FRONTEND teletype

# add the git commit
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT
ENV GIT_VERSION $GIT_COMMIT

RUN chmod 0777 /var/www/metcalf/backend/entrypoint.sh

# run entrypoint.sh
ENTRYPOINT ["/var/www/metcalf/backend/entrypoint.sh"]