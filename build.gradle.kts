plugins {
	java
	jacoco
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.sonarqube") version "4.4.1.3373"
}

group = "id.ac.ui.cs.advprog"
version = "0.0.1-SNAPSHOT"

val seleniumJavaVersion = "4.14.1"
val seleniumJupiterVersion = "5.0.1"
val webdrivermanagerVersion = "5.6.3"
val junitJupiterVersion = "5.9.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("org.postgresql:postgresql")

	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.seleniumhq.selenium:selenium-java:$seleniumJavaVersion")
	testImplementation("io.github.bonigarcia:selenium-jupiter:$seleniumJupiterVersion")
	testImplementation("io.github.bonigarcia:webdrivermanager:$webdrivermanagerVersion")
	testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

tasks.register<Test>("unitTest") {
	description = "Runs unit tests"
	group = "verification"

	filter {
		excludeTestsMatching("*FunctionalTest")
	}
}

tasks.register<Test>("functionalTest") {
	description = "Runs unit tests"
	group = "verification"

	filter {
		includeTestsMatching("*FunctionalTest")
	}
}

tasks.test {
	filter {
		excludeTestsMatching("*FunctionalTest")
	}
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
	}
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

sonarqube {
	properties {
		property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY") ?: "B09-Adpro_udehnih-auth")
		property("sonar.organization", System.getenv("SONAR_ORGANIZATION") ?: "b09-adpro")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.sources", listOf("src/main/java"))
		property("sonar.tests", listOf("src/test/java"))
		property("sonar.java.binaries", listOf("build/classes/java/main"))
		property("sonar.java.test.binaries", listOf("build/classes/java/test"))
		property("sonar.java.libraries", listOf("build/libs/*.jar"))
		property("sonar.coverage.jacoco.xmlReportPaths", listOf("build/reports/jacoco/test/jacocoTestReport.xml"))
		property("sonar.gradle.skipCompile", "true")
	}
}