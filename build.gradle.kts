import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "3.2.3"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("jvm") version "1.9.22"
  kotlin("plugin.spring") version "1.9.22"

  id("com.adarshr.test-logger") version "4.0.0"
  id("org.jmailen.kotlinter") version "4.2.0"

  id("com.gorylenko.gradle-git-properties") version "2.4.1"
  id("com.google.cloud.tools.jib") version "3.4.1"
}

group = "s4got10dev.crypto"

version =
  if (project.hasProperty("version")) {
    project.property("version") ?: "unspecified"
  } else {
    "1.0.0-SNAPSHOT"
  }

java {
  sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
  mavenCentral()
}

dependencies {
  // spring
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  // kotlin
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  // security
  implementation("com.auth0:java-jwt:4.4.0")
  // monitoring
  implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
  implementation("io.micrometer:micrometer-tracing-bridge-brave")
  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  // openapi
  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.4.0")
  // db
  implementation("org.liquibase:liquibase-core")
  implementation("org.postgresql:r2dbc-postgresql")
  runtimeOnly("org.postgresql:postgresql")
  //tools
  // dev
  developmentOnly("org.springframework.boot:spring-boot-devtools")
  // test
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:r2dbc")
  testImplementation("org.awaitility:awaitility-kotlin:4.1.1")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "21"
  }
}

// region tests
tasks.withType<Test> {
  useJUnitPlatform()
}

testlogger {
  theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
  slowThreshold = 5000
  showFullStackTraces = true
  showSimpleNames = false
}

kotlinter {
  failBuildWhenCannotAutoFormat = false
  ignoreFailures = true
  reporters = arrayOf("plain")
}
// endregion

// region jib
configure<com.google.cloud.tools.jib.gradle.JibExtension> {
  from {
    image = "eclipse-temurin:21-jre-alpine"
  }
  to {
    image = "s4got10dev/crypto/${project.name}:${if (version != "unspecified") "$version" else "latest"}"
  }
  container {
    creationTime = "USE_CURRENT_TIMESTAMP"
    ports = listOf("8080", "8081")

    jvmFlags =
      listOf(
        "-server",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=100",
        "-XX:+UseStringDeduplication",
        "-XX:+UseContainerSupport",
        "-XX:InitialRAMPercentage=50",
        "-XX:MinRAMPercentage=25",
        "-XX:MaxRAMPercentage=90",
      )
  }
}
// endregion

