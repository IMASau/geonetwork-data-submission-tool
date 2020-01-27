# first stage build an alpine image with all dependencies
FROM alpine:3.10 as builder

# add build dependencies
RUN apk add \
  libxml2-dev  \
  libxslt-dev \
  python3-dev \
  gcc \
  libffi-dev \
  musl-dev \
  openssl-dev  \
  postgresql-dev \
  libjpeg-turbo-dev

# upgrade pip and setuptools
RUN pip3 install --upgrade pip \
 && pip3 install --upgrade setuptools

# install dependencies into /install
COPY backend/requirements.txt \
     backend/prod_requirements.txt \
     /tmp/
RUN mkdir /install \
 && pip3 install --root=/install --no-warn-script-location -r /tmp/prod_requirements.txt

############################################################
# Final Image
FROM alpine:3.10

# install runtime dependencies
RUN apk add --no-cache \
      libjpeg-turbo \
      libpq \
      libxml2 \
      libxslt \
      python3

# Install supercronic
RUN export SUPERCRONIC_URL=https://github.com/aptible/supercronic/releases/download/v0.1.9/supercronic-linux-amd64 \
 && export SUPERCRONIC=supercronic-linux-amd64 \
 && export SUPERCRONIC_SHA1SUM=5ddf8ea26b56d4a7ff6faecdd8966610d5cb9d85 \
 && wget "$SUPERCRONIC_URL" \
 && echo "${SUPERCRONIC_SHA1SUM}  ${SUPERCRONIC}" | sha1sum -c - \
 && chmod +x "$SUPERCRONIC" \
 && mv "$SUPERCRONIC" "/usr/local/bin/${SUPERCRONIC}" \
 && ln -s "/usr/local/bin/${SUPERCRONIC}" /usr/local/bin/supercronic

# copy python pkgs and scripts
COPY --from=builder /install/ /

# install django app
COPY backend/ /data-submission-tool/backend/
# install frontend resources
COPY frontend/resources/ /data-submission-tool/frontend/resources/

# install media files
COPY xml_template/Metadata_Template.xml \
     xml_template/tern_template_spec.json \
     /data-submission-tool/xml_template/

# setup crontab
COPY docker-files/crontab \
     /etc/supercronic/crontab

# install entrypoint script
COPY docker-files/docker_entrypoint.sh /docker_entrypoint.sh

# create media and static folders
RUN mkdir -p /data/media \
 && mkdir /data/static

# add the git commit
ARG GIT_VERSION=unspecified
LABEL git_commit=$GIT_VERSION
ENV GIT_VERSION $GIT_VERSION

WORKDIR /data-submission-tool/backend

ENTRYPOINT ["/docker_entrypoint.sh"]
