#!/bin/sh

curl -o /tmp/application.properties -s "http://metadata.google.internal/computeMetadata/v1/instance/attributes/application-properties" -H "Metadata-Flavor: Google"

docker run -v /tmp/application.properties:/app/application.properties -p 80:8080 -t ninjaben/word-herd-server
