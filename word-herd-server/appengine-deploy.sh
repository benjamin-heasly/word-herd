#!/bin/bash

# point client app to api server
export REACT_APP_WORD_HERD_API_ROOT=https://word-herd-dot-triple-dip.appspot.com

# build client into server and deploy to appengine
./gradlew clean appengineDeploy
