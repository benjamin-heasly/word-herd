#!/bin/bash

#echo ""
#echo "Reserving a static ip address"
#echo ""
#gcloud compute addresses create word-herd-server-ip \
#    --region us-east1
#35.237.25.243


#echo ""
#echo "Opening firewall for HTTP"
#echo ""
#gcloud --quiet compute firewall-rules create default-allow-http-80 \
#  --allow tcp:80 \
#  --source-ranges 0.0.0.0/0 \
#  --target-tags word-herd-server \
#  --description "Allow port 80 access to word-herd-server"


echo ""
echo "Deleting previous instance"
echo ""
gcloud --quiet compute instances delete word-herd-server \
  --zone us-east1-b \


echo ""
echo "Building new image"
echo ""
export REACT_APP_WORD_HERD_API_ROOT=http://www.tripledip.ninja
./gradlew clean build docker dockerPush


echo ""
echo "Creating instance"
echo ""
gcloud compute instances create word-herd-server \
  --image-family cos-stable \
  --image-project cos-cloud \
  --machine-type g1-small \
  --scopes "userinfo-email,cloud-platform" \
  --metadata-from-file startup-script=gce-instance-startup.sh,application-properties=gce-application.properties \
  --address 35.237.25.243 \
  --zone us-east1-b \
  --tags word-herd-server

echo ""
echo "Waiting for service to come up"
echo ""
curl --connect-timeout 60 http://35.237.25.243/actuator/health
