plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.0.0"
    jacoco
}

group = "dev.luisvives"
version = "0.0.1-SNAPSHOT"
description = "TrabajoProgramacionSegundo"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    // Validación
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    //http
    implementation("org.springframework.boot:spring-boot-starter-web")
    //test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.h2database:h2") // base de datos a usar, puede ser otra
    // Websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // GraphQL
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")

}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)   // ver en navegador
        xml.required.set(true)    // útil para CI/CD
        csv.required.set(false)
    }

    // Solo incluir los paquetes repository, service y controller
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include(

                )
            }
        })
    )

    // Asignación correcta en Kotlin DSL
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files(layout.buildDirectory.file("jacoco/test.exec")))
}
