FROM nginx:latest

ENV environment production \
    debug false

COPY ./docker/web/default.conf /etc/nginx/conf.d/default.conf
COPY ./docker/web/entrypoint.sh /
RUN chmod +x /entrypoint.sh

COPY ./web/build/ /usr/share/nginx/html/

EXPOSE 80

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]