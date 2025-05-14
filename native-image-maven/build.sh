#!/bin/bash

# setting up environment
export FREEMARKER_GAE_JAR=../native-image/lib/freemarker-gae-2.3.35-SNAPSHOT.jar
export BASEDIR=.
export CP=${FREEMARKER_GAE_JAR}:target/classes/.

mvn org.apache.maven.plugins:maven-install-plugin:3.1.4:install-file  -Dfile=${FREEMARKER_GAE_JAR} \
                                                                              -DgroupId=org.freemarker \
                                                                              -DartifactId=freemarker-gae \
                                                                              -Dversion=2.3.35-SNAPSHOT \
                                                                              -Dpackaging=jar

# just in time application build
mvn clean install

# test application
java -cp ${CP} FreeMarkerGraalVMSample

# aot application build
mvn clean install -Pnative

# running the application
./target/freemarkergraalvmsample