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
