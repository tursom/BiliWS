import com.google.protobuf.gradle.*
import java.util.*

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

try {
  val properties = Properties()
  properties.load(rootProject.file("local.properties").inputStream())
  properties.forEach { (k, v) ->
    rootProject.ext.set(k.toString(), v)
  }
} catch (e: Exception) {
}

plugins {
  kotlin("jvm") version "1.5.20"
  `maven-publish`
  id("com.google.protobuf") version "0.8.16"
}

group = "cn.tursom"
version = "1.0-SNAPSHOT"

repositories {
  // mavenLocal()
  mavenCentral()
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

  //implementation "cn.tursom:TursomServer:0.1"
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

  testImplementation(group = "junit", name = "junit", version = "4.12")
  testImplementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")
  testImplementation("ch.qos.logback:logback-classic:1.2.9")
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

// skip test
if (project.gradle.startParameter.taskNames.firstOrNull { taskName ->
    ":test" in taskName
  } == null) {
  tasks {
    test { enabled = false }
    testClasses { enabled = false }
    compileTestJava { enabled = false }
    compileTestKotlin { enabled = false }
    processTestResources { enabled = false }
  }
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
  val artifactoryUser: String by rootProject
  val artifactoryPassword: String by rootProject
  repositories {
    maven {
      val releasesRepoUrl = uri("https://nvm.tursom.cn/repository/maven-releases/")
      val snapshotRepoUrl = uri("https://nvm.tursom.cn/repository/maven-snapshots/")
      url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else releasesRepoUrl
      credentials {
        username = artifactoryUser
        password = artifactoryPassword
      }
    }
  }
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