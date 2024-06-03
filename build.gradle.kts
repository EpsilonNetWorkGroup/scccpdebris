/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.kyori.blossom") version "1.3.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper.packetevents:spigot:2.3.0")
}

group = "net.playl.scccpdebris"
version = "1.7.4"
description = "scccpdebris"

tasks {
    processResources {
        val props = mapOf(
            "version" to version,
        )
        filesMatching("plugin.yml") {
            expand(props)
        }

    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named("build") {
    finalizedBy("shadowJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
