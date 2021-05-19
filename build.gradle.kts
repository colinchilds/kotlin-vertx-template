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
    kotlin("jvm") version "1.5.0"
    id("com.moowork.node") version "1.3.1"
    id("com.github.johnrengelman.shadow") version "5.1.0"
//    id("com.palantir.docker") version "0.22.1"
//    id("com.palantir.docker-run") version "0.22.1"
    application
}

val kotlinVersion = "1.5.0"
val vertxVersion = "4.0.3"
val nettyVersion = "4.1.60.Final" //Must update this as vertx does to get native transports
val junitVersion = "5.3.2"

dependencies {
    implementation(project(":Koddle"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-web-client:$vertxVersion")
    implementation("io.vertx:vertx-web-api-contract:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-config:$vertxVersion")
    implementation("io.vertx:vertx-pg-client:$vertxVersion")
    implementation("io.vertx:vertx-auth-jwt:$vertxVersion")

    implementation("org.flywaydb:flyway-core:6.0.0")
    implementation("org.postgresql:postgresql:42.2.20")
    implementation("org.koin:koin-core:2.0.1")
    implementation("org.koin:koin-core-ext:2.0.1")
    implementation("org.slf4j:slf4j-jdk14:1.7.28")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // Try to use a native transport for Netty, for increased performance, if available for the current OS type.
    // See https://netty.io/wiki/native-transports.html
    val currentOS = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()
    val currentArch = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentArchitecture()
    if (currentArch.isAmd64) {
        if (currentOS.isLinux) {
            implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-x86_64")
        } else if (currentOS.isMacOsX || currentOS.isFreeBSD) {
            implementation("io.netty:netty-transport-native-kqueue:$nettyVersion:osx-x86_64")
            implementation("io.netty:netty-resolver-dns-native-macos:$nettyVersion:osx-x86_64")
        } else {
            println(
                "No Netty native transport available for OS \"${currentOS.name}\". Netty will fall back to using the " +
                        "NIO transport."
            )
        }
    } else {
        println(
            "No Netty native transport available for architecture \"${currentArch.name}\". Netty will fall back " +
                    "to using the NIO transport."
        )
    }
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
        useJUnitPlatform()
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