import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("net.mamoe.mirai-console") version "2.14.0"
    java
}

group = "games.go"
version = "0.1.0"

repositories {
    maven{ url = uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    maven{ url = uri("https://www.jitpack.io")}
    jcenter()
    mavenCentral()
    mavenLocal()
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

    //implementation(kotlin("stdlib-jdk8"))
}

tasks.getByName<Jar>("jar") {
    manifest {
        attributes(mapOf(
            "Main-Class" to "KotlinMain",
            "Author" to "Minxyzgo"
        ))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}