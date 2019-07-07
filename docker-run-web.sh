#!/usr/bin/env bash

docker run -p 81:80 \
 -d \
 -e API_URL=http://beebox02:9000/api \
 grozeille/document-analysis-web:1.0