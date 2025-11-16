

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "9.0.0"
    jacoco
    id("org.jetbrains.dokka") version "2.1.0"
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
    // 3. AÑADIR DEPENDENCIAS DE TESTCONTAINERS
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.springframework.security:spring-security-test")
    // JWT - using Auth0 JWT
    implementation("com.auth0:java-jwt:4.4.0")
    //bbdd
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // Websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // GraphQL
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation(platform("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.1.0"))

}
tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/test/html"))
    }

    doLast {
        // Intentar copiar el CSS personalizado desde /custom/report.css
        val customCssFile = file("${projectDir}/custom/report/report.css")
        val jacocoHtmlDir = file("${layout.buildDirectory.get()}/reports/jacoco/test/html")
        val targetCssFiles = listOf(
            file("${jacocoHtmlDir}/jacoco-resources/report.css"),
            file("${jacocoHtmlDir}/.resources/report.css"),
            file("${jacocoHtmlDir}/report.css")
        )

        if (customCssFile.exists()) {
            println("✅ CSS personalizado encontrado en: ${customCssFile.absolutePath}")

            // Copiar a todas las ubicaciones posibles de JaCoCo
            targetCssFiles.forEach { targetFile ->
                try {
                    targetFile.parentFile.mkdirs()
                    customCssFile.copyTo(targetFile, overwrite = true)
                    println("✅ CSS copiado a: ${targetFile.absolutePath}")
                } catch (e: Exception) {
                    println("⚠️  No se pudo copiar CSS a ${targetFile.absolutePath}: ${e.message}")
                }
            }

            println("🎨 CSS personalizado aplicado correctamente")
        } else {
            println("⚠️  CSS personalizado no encontrado en: ${customCssFile.absolutePath}")
            println("📋 Usando CSS por defecto de JaCoCo")
            println("💡 Para usar CSS personalizado, coloca el archivo en: custom/report.css")
        }

        println("📊 Reporte JaCoCo generado en: ${jacocoHtmlDir}/index.html")
    }
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
                exclude(
                    "**/config/**",
                    "**/email/**",
                    "**/notificaciones/**",
                    "**/handler/**",

                )
            }
        })
    )

    // Asignación correcta en Kotlin DSL
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(files(layout.buildDirectory.file("jacoco/test.exec")))
}