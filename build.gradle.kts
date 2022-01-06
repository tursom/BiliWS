import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

buildscript {
  repositories {
    maven {
      url = uri("https://nvm.tursom.cn/repository/maven-public/")
    }
  }
  dependencies {
    classpath("cn.tursom:ts-gradle:1.0-SNAPSHOT") { isChanging = true }
  }
  configurations {
    all {
      resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
      resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
  }
}

apply(plugin = "ts-gradle")

plugins {
  kotlin("jvm") version "1.5.20"
  `maven-publish`
  id("com.google.protobuf") version "0.8.16"
}

group = "cn.tursom"
version = "1.0-SNAPSHOT"

repositories {
  maven {
    url = uri("https://nvm.tursom.cn/repository/maven-public/")
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
    resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
  }
}

dependencies {
  api(kotlin("stdlib-jdk8"))

  val tursomServerVersion = "1.0-SNAPSHOT"
  api("cn.tursom", "ts-ws-client", tursomServerVersion)
  api("cn.tursom", "ts-async-http", tursomServerVersion)
  api("cn.tursom", "ts-datastruct", tursomServerVersion)
  api("cn.tursom", "ts-core", tursomServerVersion)
  api("cn.tursom", "ts-log", tursomServerVersion)

  api(group = "com.google.protobuf", name = "protobuf-java", version = "3.17.3")
  api(group = "org.slf4j", name = "slf4j-api", version = "1.7.29")
  implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.3")
  implementation("com.google.code.gson:gson:2.8.9")
  implementation(group = "org.mapdb", name = "mapdb", version = "3.0.8")
  implementation(group = "io.netty", name = "netty-tcnative-boringssl-static", version = "2.0.46.Final")

  testImplementation(group = "junit", name = "junit", version = "4.12")
  testImplementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.10")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

// skip test
if (project.gradle.startParameter.taskNames.firstOrNull { taskName ->
    taskName.endsWith(":test")
  } == null) {
  tasks.withType<Test> { enabled = false }
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.17.1"
  }
  generatedFilesBaseDir = "$projectDir/src"
  plugins {
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.38.0"
    }
  }
  generateProtoTasks {
    all().forEach {
      it.plugins {
        id("grpc") {
          outputSubDir = "java"
        }
      }
    }
  }
}