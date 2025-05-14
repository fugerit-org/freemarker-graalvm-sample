# FreeMarker GraalVM Sample native image

## Build

```shell
./build.sh
```

## Run

```shell
./build/freemarkergraalvmsample 
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

### Maven build file (POM)

When using the Maven build system, most configuration will be included in pom.xml inside the  *org.graalvm.buildtools:native-maven-plugin*.

Refer to [native-maven-plugin documentation](https://graalvm.github.io/native-build-tools/latest/end-to-end-maven-guide.html) for further information.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>native-image-maven</artifactId>
	<groupId>org.fugerit.java.demo</groupId>

	<name>Proof Of Concept for Apache FreeMarker and GraalVM - Maven</name>
	<description>Proof Of Concept for Apache FreeMarker Pull Request #121 - Added GraalVM native support to Apache FreeMarker</description>
	<version>1.0.0-SNAPSHOT</version>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
			<distribution>repo</distribution>
		</license>
	</licenses>

	<properties>
		<maven.compiler.release>21</maven.compiler.release>
		<freemarker-gae-version>2.3.35-SNAPSHOT</freemarker-gae-version>
		<native.maven.plugin.version>0.10.6</native.maven.plugin.version>
		<jupipter-junit-version>5.12.2</jupipter-junit-version>
	</properties>	

	<organization>
		<url>https://www.fugerit.org</url>
		<name>Fugerit</name>
	</organization>
	
	<url>https://www.fugerit.org/</url>
	
	<scm>
		<connection>scm:git:git://github.com/fugerit-org/poc-freemarker-graalvm.git</connection>
		<developerConnection>scm:git:ssh://github.com/fugerit-org/poc-freemarker-graalvm.git</developerConnection>
		<url>https://github.com/fugerit-org/poc-freemarker-graalvm.git</url>
		<tag>HEAD</tag>
	</scm>

	<issueManagement>
		<system>GitHub</system>
		<url>https://github.com/fugerit-org/freemarker-graalvm-sample/issues</url>
	</issueManagement>

	<dependencies>

		<dependency>
			<groupId>org.freemarker</groupId>
			<artifactId>freemarker-gae</artifactId>
			<version>${freemarker-gae-version}</version>
		</dependency>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-api</artifactId>
			<version>${jupipter-junit-version}</version>
			<scope>test</scope>
		</dependency>

	</dependencies>

	<profiles>

		<profile>

			<id>native</id>

			<build>

				<plugins>

					<plugin>
						<groupId>org.graalvm.buildtools</groupId>
						<artifactId>native-maven-plugin</artifactId>
						<version>${native.maven.plugin.version}</version>
						<extensions>true</extensions>
						<executions>
							<execution>
								<id>build-native</id>
								<goals>
									<goal>compile-no-fork</goal>
								</goals>
								<phase>package</phase>
							</execution>
						</executions>
						<configuration>
							<buildArgs>
								<buildArg>-H:Path=target</buildArg>
								<buildArg>-H:Name=freemarkergraalvmsample</buildArg>
								<buildArg>-H:IncludeResources=^templates/.*</buildArg>
								<buildArg>-H:ReflectionConfigurationFiles=src/main/resources/custom-reflect-config.json</buildArg>
							</buildArgs>
							<mainClass>FreeMarkerGraalVMSample</mainClass>
						</configuration>
					</plugin>

				</plugins>

			</build>

		</profile>

	</profiles>
		
</project>
```

### Build the native image

```shell
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
```