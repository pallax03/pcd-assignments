plugins {
    java
    scala
    application
}

repositories {
    mavenCentral()
}

val scala3Version = "3.8.3"
val pekkoVersion = "1.6.0"
val rabbitMQVersion = "5.25.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.scala-lang:scala3-library_3:$scala3Version")
    implementation("org.apache.pekko:pekko-actor_3:$pekkoVersion")
    testImplementation("org.apache.pekko:pekko-testkit_3:$pekkoVersion")
    implementation("com.typesafe.scala-logging:scala-logging_3:3.9.5")
    implementation("ch.qos.logback:logback-classic:1.5.32")

    implementation("org.apache.pekko:pekko-cluster-typed_3:$pekkoVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding_3:$pekkoVersion")
    implementation("org.apache.pekko:pekko-serialization-jackson_3:$pekkoVersion")


    implementation("com.rabbitmq:amqp-client:$rabbitMQVersion")
}

tasks.register<JavaExec>("runCluster") {
    group = "application"
    description = "Run Cluster"

    standardInput = System.`in`

    mainClass.set("cluster.App")
    classpath = sourceSets["main"].runtimeClasspath

    systemProperties(System.getProperties().mapKeys { it.key.toString() })
}
tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Run Client TicTacToe"

    standardOutput = System.out
    errorOutput = System.err

    mainClass.set("rmi.RunClient")
    classpath = sourceSets["main"].runtimeClasspath

    systemProperties(System.getProperties().mapKeys { it.key.toString() })
}
tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Run Server TicTacToe"

    standardOutput = System.out
    errorOutput = System.err

    mainClass.set("rmi.RunServer")
    classpath = sourceSets["main"].runtimeClasspath

    systemProperties(System.getProperties().mapKeys { it.key.toString() })
}

tasks.register<JavaExec>("runNode") {
    group = "application"
    description = "Run Node Client CriticalSection"

    standardInput = System.`in`
    standardOutput = System.out

    mainClass.set("rabbitmq.Client")
    classpath = sourceSets["main"].runtimeClasspath

    systemProperties(System.getProperties().mapKeys { it.key.toString() })
}

tasks.withType<Test> {
    useJUnitPlatform()
}
