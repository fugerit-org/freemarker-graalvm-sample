# FreeMarker GraalVM Sample native image - Micronaut

## Build

```shell
./gradlew nativeCompile
```

## Run

```shell
./build/native/nativeCompile/freemarkergraalvmsample 
```

```shell
curl http://localhost:8080/freemarker/sample.xml
```

## Output

Expected output should be

```xml
<freemarker-graalvm-sample>
<freemarker-version>2.3.35-nightly</freemarker-version>
<description>FreeMarkerGraalVMMavenSample Micronaut</description>
</freemarker-graalvm-sample>
```

## Guide

Here is a sample usage guide for ApacheFreeMarker + GraalVM.

To run the sample in classic Just In Time Way, we only need :

* FreeMarkerController.java
* sample.ftl

### FreeMarkerController.java sample class

```java
package org.fugerit.java.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/freemarker")
public class FreeMarkerController {

    private static final Logger log = LoggerFactory.getLogger(FreeMarkerController.class);

    // Data class as static inner class
    public static class Data {
        private String description;
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(FreeMarkerController.class, "/templates");
        Template template = cfg.getTemplate(templatePath);
        template.process(dataModel, writer);
    }

    @GetMapping(value = "/sample.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String runSample() {
        try (StringWriter writer = new StringWriter()) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription("FreeMarkerGraalVMSample Micronaut");
            dataModel.put("data", data);
            handleTemplate(writer, "sample.ftl", dataModel);
            return writer.toString();
        } catch (Exception e) {
            String errorMessage = "error : %s".formatted(e.getMessage());
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
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
  "name" : "org.fugerit.java.demo.FreeMarkerController$Data",
  "methods" : [ {
    "name" : "<init>",
    "parameterTypes" : [ ]
  },{
    "name" : "getDescription",
    "parameterTypes" : [ ]
  } ]
}]
```

### Gradle build file

Refers to [Gradle documentation](https://spring.io/guides/gs/spring-boot) to configure build.gradle.kts :

```groovy
plugins {
    id("io.micronaut.application") version "4.5.3"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.micronaut.aot") version "4.5.3"
}

version = "0.1"
group = "org.fugerit.java.demo"

repositories {
    mavenCentral()
}

ext {
    freemarkerGaeVersion = '2.3.35-SNAPSHOT'
}

dependencies {
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    annotationProcessor("io.micronaut.spring:micronaut-spring-annotation")
    annotationProcessor("io.micronaut.spring:micronaut-spring-web-annotation")
    implementation("io.micronaut:micronaut-http-server")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.micronaut:micronaut-http-server")
    implementation files("../native-image/lib/freemarker-gae-${freemarkerGaeVersion}.jar")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("io.micronaut.spring:micronaut-spring-web")
    testAnnotationProcessor("io.micronaut.spring:micronaut-spring-web-annotation")
    testImplementation("io.micronaut:micronaut-http-client")
}


application {
    mainClass = "org.fugerit.java.demo.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}


graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("org.fugerit.java.demo.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


tasks.named("dockerfileNative") {
    jdkVersion = "21"
}

graalvmNative {
    binaries {
        main {
            imageName = 'freemarkergraalvmsample'
            def reflectConfigFile = file('src/main/resources/custom-reflect-config.json')
            buildArgs.addAll([
                    '-H:Name=freemarkergraalvmsample',
                    '-H:IncludeResources=^templates/.*',
                    "-H:ReflectionConfigurationFiles=${reflectConfigFile.absolutePath}"
            ])
        }
    }
}
```

## Original Micronaut 4.8.2 Documentation

- [User Guide](https://docs.micronaut.io/4.8.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.8.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.8.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)


## Feature spring documentation

- [Micronaut Spring Framework Annotations documentation](https://micronaut-projects.github.io/micronaut-spring/latest/guide/index.html)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)


