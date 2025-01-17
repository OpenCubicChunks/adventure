plugins {
  id("adventure.common-conventions")
  kotlin("jvm") version "1.6.10"
}

kotlin {
  explicitApi()
  coreLibrariesVersion = "1.4.32"
  target {
    compilations.configureEach {
      kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.4"
      }
    }
  }
}

dependencies {
  api(project(":adventure-api"))
  implementation(kotlin("stdlib-jdk8"))
  testImplementation(kotlin("test-junit5"))
}

applyJarMetadata("net.kyori.adventure.extra.kotlin")
