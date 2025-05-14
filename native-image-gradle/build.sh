#!/bin/bash

# setting up environment
export FREEMARKER_GAE_JAR=../native-image/lib/freemarker-gae-2.3.35-SNAPSHOT.jar
export BASEDIR=.
export CP=${FREEMARKER_GAE_JAR}:build/classes/:build/resources/

# just in time application build
gradle clean build

# test application
java -cp ${CP} FreeMarkerGraalVMSample

# aot application build
gradle build nativeCompile

# running the application
./build/native/nativeCompile/freemarkergraalvmsample