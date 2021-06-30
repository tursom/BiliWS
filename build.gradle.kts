import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.5.20"
  id("maven")
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