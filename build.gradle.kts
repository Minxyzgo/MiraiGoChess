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

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("com.beust:klaxon:5.5")
}