
import cn.tursom.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    maven {
      url = uri("https://jmp.mvn.tursom.cn:20080/repository/maven-public/")
    }
  }
  dependencies {
    classpath("cn.tursom:ts-gradle-env:1.1-SNAPSHOT") { isChanging = true }
    classpath("cn.tursom:ts-gradle-repos:1.1-SNAPSHOT") { isChanging = true }
    classpath("cn.tursom:ts-gradle-publish:1.1-SNAPSHOT") { isChanging = true }
  }
  configurations {
    all {
      resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
      resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
  }
}

group = "cn.tursom"
version = "1.1-SNAPSHOT"

apply(plugin = "ts-gradle-env")
apply(plugin = "ts-gradle-repos")
apply(plugin = "ts-gradle-publish")

plugins {
  kotlin("jvm") version "1.9.22"
  `maven-publish`
  id("com.google.protobuf") version "0.9.4"
}

useTursomRepositories()

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
  ts_ws_client
  ts_async_http
  ts_datastruct
  ts_core
  ts_log
  ts_coroutine
  ts_observer

  api(group = "com.google.protobuf", name = "protobuf-java", version = "3.25.1")
  api(group = "org.slf4j", name = "slf4j-api", version = "1.7.29")
  implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.3")
  implementation("com.google.code.gson:gson:2.8.9")
  implementation(group = "org.mapdb", name = "mapdb", version = "3.0.8")
  implementation(group = "io.netty", name = "netty-tcnative-boringssl-static", version = "2.0.46.Final")

  testImplementation(group = "junit", name = "junit", version = "4.13.1")
  //testImplementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.10")
  testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.4.12")
  testImplementation(group = "xerces", name = "xercesImpl", version = "2.12.2")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "21"
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
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
  //generatedFilesBaseDir = "$projectDir/src"
  //plugins {
  //  id("grpc") {
  //    artifact = "io.grpc:protoc-gen-grpc-java:1.38.0"
  //  }
  //}
  //generateProtoTasks {
  //  all().forEach {
  //    it.plugins {
  //      id("grpc") {
  //        outputSubDir = "java"
  //      }
  //    }
  //  }
  //}
}