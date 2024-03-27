val ktorVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val jbcryptVersion: String by project
val databaseDriverVersion: String by project
val logbackVersion: String by project
val kotlinVersion: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_11)
        localImageName.set("sample-docker-image")
        imageTag.set("0.0.1-preview")

    }
    fatJar {
        archiveFileName.set("yt-service.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/UndetectedChromedriver.jar"))
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
    implementation("io.ktor:ktor-server-core-jvm:2.2.4")
    implementation("io.ktor:ktor-serialization-gson-jvm:2.2.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")

    implementation("org.seleniumhq.selenium:selenium-java:4.14.1")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.14.1")
    implementation("org.jsoup:jsoup:1.16.2")

    implementation("com.alibaba:fastjson:2.0.28")
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")

    implementation("org.flywaydb:flyway-core:9.22.3")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")
    implementation("org.postgresql:postgresql:$databaseDriverVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}