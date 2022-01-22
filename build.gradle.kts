plugins {
    kotlin("jvm") version "1.5.10"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(files("libs/TwsApi.jar"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}