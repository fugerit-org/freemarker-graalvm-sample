# FreeMarker GraalVM Sample native image - Gradle KTS

## Build

```shell
./build.sh
```

## Run

```shell
./build/native/nativeCompile/freemarkergraalvmsample
```

## Output

Expected output should be 

```xml
<freemarker-graalvm-sample>
<freemarker-version>2.3.35-nightly</freemarker-version>
<description>FreeMarkerGraalVMMavenSample</description>
</freemarker-graalvm-sample>
```

## Guide

Here is a sample usage guide for ApacheFreeMarker + GraalVM.

To run the sample in classic Just In Time Way, we only need :

* FreeMarkerGraalVMMavenSample.java
* sample.ftl

But for the Ahead Of Time application with GraalVM some additional configuration is required : 

* custom-reflect-config.json

### FreeMarkerGraalVMMavenSample.java sample class

```java
import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerGraalVMMavenSample {

    private final static Logger LOG = Logger.getLogger(FreeMarkerGraalVMMavenSample.class.getName());

    /* data model */
    public class Data {
        private String description;
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_34 );
        cfg.setClassForTemplateLoading( FreeMarkerGraalVMMavenSample.class, "/templates" );
        Template template = cfg.getTemplate( templatePath );
        template.process( dataModel, writer );
    }

    public void runSample() {
        try ( StringWriter writer = new StringWriter() ) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription( "FreeMarkerGraalVMMavenSample" );
            dataModel.put("data", data);
            handleTemplate( writer, "sample.ftl", dataModel );
            LOG.info( writer.toString() );
        } catch (Exception e) {
            LOG.error( e.getMessage(), e );
        }
    }

    public static void main(String[] args) {
        FreeMarkerGraalVMMavenSample sample = new FreeMarkerGraalVMMavenSample();
        sample.runSample();
    }

}
```

### Apache FreeMarker template

```ftl
<freemarker-graalvm-sample>
    <freemarker-version>${.version}</freemarker-version>
    <description>${data.description}</description>
</freemarker-graalvm-sample>
```

### Reflection configuration, custom-reflect-config.json

Refers to [Reflection in Native Image](https://www.graalvm.org/jdk21/reference-manual/native-image/dynamic-features/Reflection/) guide

```json
[{
  "name" : "FreeMarkerGraalVMMavenSample$Data",
  "methods" : [ {
    "name" : "<init>",
    "parameterTypes" : [ ]
  },{
    "name" : "getDescription",
    "parameterTypes" : [ ]
  } ]
}]
```

### Gradke build configuration

When using the Gradle build system, most configuration will be included in build.gradle.kts by adding  *org.graalvm.buildtools.native*.

Refer to [native-build-tools gradle documentation](https://graalvm.github.io/native-build-tools/latest/end-to-end-gradle-guide.html) for further information.

```kotlin
plugins {
    java
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "org.fugerit.java.demo"
version = "1.0.0-SNAPSHOT"
description = "Proof Of Concept for Apache FreeMarker Pull Request #121 - Added GraalVM native support to Apache FreeMarker"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

val freemarkerGaeVersion = "2.3.35-SNAPSHOT"
val junitJupiterVersion = "5.12.2"

dependencies {
    implementation(files("../native-image/lib/freemarker-gae-$freemarkerGaeVersion.jar"))
    //implementation("org.freemarker:freemarker-gae:$freemarkerGaeVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.test {
    useJUnitPlatform()
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("freemarkergraalvmsample")
            val reflectConfigFile = layout.projectDirectory.file("src/main/resources/custom-reflect-config.json")
            buildArgs.addAll(
                listOf(
                    "-H:Name=freemarkergraalvmsample",
                    "-H:IncludeResources=^templates/.*",
                    "-H:ReflectionConfigurationFiles=${reflectConfigFile.asFile.absolutePath}"
                )
            )
            mainClass.set("FreeMarkerGraalVMSample")
        }
    }
}
```

### Build the native image

```shell
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
```