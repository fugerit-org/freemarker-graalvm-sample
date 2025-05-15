# FreeMarker GraalVM Sample native image - Spring Boot

## Build

```shell
gradle nativeCompile
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
<description>FreeMarkerGraalVMMavenSample SpringBoot</description>
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
            data.setDescription("FreeMarkerGraalVMSample SpringBoot");
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

```kotlin
plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "org.fugerit.java.demo"
version = "0.0.1-SNAPSHOT"

val freemarkerGaeVersion = "2.3.35-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation(files("../native-image/lib/freemarker-gae-$freemarkerGaeVersion.jar"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

graalvmNative {
	binaries {
		named("main") {
			imageName.set("freemarkergraalvmsample")
			val reflectConfigFile = layout.projectDirectory.file("src/main/resources/custom-reflect-config.json")
			buildArgs.addAll(
				listOf(
					"-H:IncludeResources=^templates/.*",
					"-H:ReflectionConfigurationFiles=${reflectConfigFile.asFile.absolutePath}"
				)
			)
		}
	}
}
```
