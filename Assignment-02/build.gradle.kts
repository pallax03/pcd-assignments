plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:5.0.11")
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
