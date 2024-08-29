plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "io.github.apwlq"
version = "v1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.1")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("sow_client.jar")
    manifest {
        attributes(
            "Main-Class" to "io.github.apwlq.sow.client.MainKt", // Main-Class를 실제 진입점으로 설정
            "Implementation-Version" to project.version // version을 MANIFEST에 추가
        )
    }
}

kotlin {
    jvmToolchain(21)
}