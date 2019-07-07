#!/usr/bin/env bash

sed -i -e "s=http://localhost:9000/api=$API_URL=g" /usr/share/nginx/html/config.json

nginx -g "daemon off;"