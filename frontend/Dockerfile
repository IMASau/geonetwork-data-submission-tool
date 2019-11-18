# Use the Nginx image
FROM clojure:openjdk-14-lein-alpine

# ADD . /app
RUN mkdir -p /var/www/metcalf/frontend

# Set the working directory to /var/www/metcalf
WORKDIR /var/www/metcalf/frontend

RUN apk update && apk add ca-certificates \
    curl \
    make \
    nginx \
    yarn \
    && rm -rf /var/cache/apk/*

COPY usermetcalf /etc/sudoers.d/metcalf

RUN adduser -D -g 'metcalf' metcalf \
    && chown -R metcalf:metcalf /var/www/metcalf \
    && mkdir -p /var/lib/nginx/logs/ \
    && chown -R metcalf:metcalf /var/lib/nginx \
    && chown -R root:root /etc/sudoers.d/metcalf \
    && chmod 0440 /etc/sudoers.d/metcalf \
    && mkdir -p /run/nginx
COPY nginx.conf /etc/nginx/nginx.conf
COPY nginx_available.conf /etc/nginx/sites-available/metcalf
COPY nginx_available.conf /etc/nginx/sites-enabled/metcalf
COPY sys.conf /etc/sysctl.d/99-sysctl.conf

RUN curl -O https://download.clojure.org/install/linux-install-1.10.1.483.sh \
    && chmod +x linux-install-1.10.1.483.sh \
    && ./linux-install-1.10.1.483.sh

# Copy the current directory contents into the container at /var/www/metcalf
COPY . /var/www/metcalf/frontend

# this is were everything build
RUN make release

RUN make copy-static

#CMD nginx -g 'daemon off;'
# run entrypoint.sh
RUN chmod 0777 /var/www/metcalf/frontend/entrypoint.sh
ENTRYPOINT ["/var/www/metcalf/frontend/entrypoint.sh"]