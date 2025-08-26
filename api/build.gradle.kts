plugins {
    id("java")
    id ("org.springframework.boot") version "3.5.3"
    id ("io.spring.dependency-management") version "1.1.7"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.6.18.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    annotationProcessor ("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}