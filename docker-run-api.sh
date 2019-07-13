#!/usr/bin/env bash

docker run -p 9000:9000 \
 -d \
 -v /mnt/sda1/document-analysis/:/opt/data \
 -e SOLR_URL=http://beebox02:8983/solr \
 -e TRANSLATION_CACHE_PATH=/opt/data/translationCache.db \
 -e GOOGLE_API_KEY=${GOOGLE_API_KEY}  \
 grozeille/document-analysis-api:1.4