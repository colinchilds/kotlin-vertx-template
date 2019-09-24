import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.moowork.gradle.node.npm.NpmTask

application {
    mainClassName = "dev.cchilds.service.MyServiceKt"
}

buildscript {
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.1.0")
    }
}

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.moowork.node") version "1.3.1"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("com.palantir.docker") version "0.22.1"
    id("com.palantir.docker-run") version "0.22.1"
    application
}

val vertxVersion = "3.8.1"

dependencies {
    implementation(project(":Koddle"))

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-web-api-contract:$vertxVersion")
    implementation("io.vertx:vertx-web-client:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-unit:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-auth-jwt:$vertxVersion")

    implementation("postgresql:postgresql:9.1-901-1.jdbc4")
    implementation("org.koin:koin-core:2.0.1")
    implementation("org.koin:koin-core-ext:2.0.1")
    implementation("org.slf4j:slf4j-jdk14:1.7.28")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.7")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.7")
    testImplementation("org.amshove.kluent:kluent:1.54")
}

repositories {
    mavenCentral()
    jcenter()
}

group = "dev.cchilds.kotlin-vertx-template"
version = "1.0-SNAPSHOT"

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
    withType<ShadowJar> {
        baseName = "kvt"
        classifier = null
        version = null
    }
}

node {
    version = "10.16.3"
    npmVersion = "6.9.0"
    download = true
    nodeModulesDir = File("src/main/frontend")
}

val buildFrontend by tasks.creating(NpmTask::class) {
    setArgs(listOf("run", "build"))
    dependsOn("npmInstall")
}

val copyToWebRoot by tasks.creating(Copy::class) {
    from("src/main/frontend/build")
    destinationDir = File("src/main/resources/webroot")
    dependsOn(buildFrontend)
}

val processResources by tasks.getting(ProcessResources::class) {
    dependsOn(copyToWebRoot)
}