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
