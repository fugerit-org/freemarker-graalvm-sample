#!/bin/bash

# setting up environment
export BASEDIR=.
export CP=./lib/freemarker-gae-2.3.35-SNAPSHOT.jar:.

# just in time application build
javac -cp ${CP} -d build ./src/FreeMarkerGraalVMSample.java

# ahead of time application build
#
# -H:IncludeResources=^templates/.*
#      will make the templates available to the native-image
#
# -H:ReflectionConfigurationFiles=./config/custom-reflect-config.json
#      will setup reflection custom configuration
native-image \
  -cp "${CP}:build" \
  -H:Path=build \
  -H:Class=FreeMarkerGraalVMSample \
  -H:IncludeResources=^templates/.* \
  -H:+UnlockExperimentalVMOptions \
  -H:ReflectionConfigurationFiles=./config/custom-reflect-config.json \
  --no-fallback \
  --report-unsupported-elements-at-runtime

# running the application
./build/freemarkergraalvmsample