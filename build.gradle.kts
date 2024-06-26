plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"

    kotlin("jvm") version "1.8.20"
}

group = "com.codeplugin"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.alibaba.fastjson2:fastjson2:2.0.47")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("com.google.guava:guava:33.0.0-jre")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2")
    type.set("IU")
    plugins.set(listOf("com.intellij.java"))
}

kotlin {
    jvmToolchain({
        17
    })
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("300.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
