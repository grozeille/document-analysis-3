FROM nginx:latest

ENV environment production \
    debug false

COPY ./docker/web/default.conf /etc/nginx/conf.d/default.conf
COPY ./docker/web/entrypoint.sh /
RUN chmod +x /entrypoint.sh

COPY ./web/build/ /usr/share/nginx/html/

EXPOSE 80
EXPOSE 443

ENV API_URL=http://localhost:9000/api

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]