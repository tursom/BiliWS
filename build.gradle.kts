import com.google.protobuf.gradle.*

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.20"
  `maven-publish`
  id("com.google.protobuf") version "0.8.16"
}

group = "cn.tursom"
version = "1.0"

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  api(kotlin("stdlib-jdk8"))

  //implementation "cn.tursom:TursomServer:0.1"
  api("cn.tursom:ts-ws-client:0.2")
  api("cn.tursom:ts-async-http:0.2")
  api("cn.tursom:ts-datastruct:0.2")
  api("cn.tursom:ts-core:0.2")
  api("cn.tursom:ts-log:0.2")

  api(group = "com.google.protobuf", name = "protobuf-java", version = "3.17.3")
  api(group = "org.slf4j", name = "slf4j-api", version = "1.7.29")
  implementation("com.google.code.gson:gson:2.8.2")
  implementation(group = "org.mapdb", name = "mapdb", version = "3.0.8")

  testImplementation(group = "junit", name = "junit", version = "4.12")
  testImplementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")
  testImplementation("ch.qos.logback:logback-classic:1.2.3")
}


tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

//打包源代码
artifacts {
  archives(tasks["kotlinSourcesJar"])
}

tasks.register("install") {
  // dependsOn(tasks["build"])
  finalizedBy(tasks["publishToMavenLocal"])
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = project.group.toString()
      artifactId = project.name
      version = project.version.toString()

      from(components["java"])
      try {
        artifact(tasks["sourcesJar"])
      } catch (e: Exception) {
        try {
          artifact(tasks["kotlinSourcesJar"])
        } catch (e: Exception) {
        }
      }
    }
  }
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