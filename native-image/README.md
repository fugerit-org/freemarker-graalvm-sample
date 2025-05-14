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

### Build the native image

```shell
#!/bin/bash

# setting up environment
export BASEDIR=.
export CP=./lib/freemarker-gae-2.3.35-SNAPSHOT.jar:.

# just in time application build
javac -cp ${CP} -d build ./src/FreeMarkerGraalVMMavenSample.java

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
  -H:Class=FreeMarkerGraalVMMavenSample \
  -H:IncludeResources=^templates/.* \
  -H:+UnlockExperimentalVMOptions \
  -H:ReflectionConfigurationFiles=./config/custom-reflect-config.json \
  --no-fallback \
  --report-unsupported-elements-at-runtime

# running the application
./build/freemarkergraalvmsample
```