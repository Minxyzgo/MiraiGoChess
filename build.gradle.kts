import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.4.32"

    id("net.mamoe.mirai-console") version "2.6.3"
    java
}

group = "games.go"
version = "0.0.1"

repositories {
    maven{ url = uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    maven{ url = uri("https://www.jitpack.io")}
    jcenter()
    mavenCentral()
    mavenLocal()
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes(mapOf(
            "Main-Class" to "KotlinMain",
            "Author" to "Minxyzgo"
        ))
    }
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven{ url = uri("https://maven.aliyun.com/repository/google") }
        maven{ url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven{ url = uri("https://maven.aliyun.com/repository/public") }
        maven{ url = uri("https://maven.aliyun.com/repository/jcenter") }
    }
}

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("com.beust:klaxon:5.5")
}