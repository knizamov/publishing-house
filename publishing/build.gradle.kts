plugins {
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.spring") version "1.4.32"
    id("com.bnorm.power.kotlin-power-assert") version "0.7.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("am.ik.yavi:yavi:0.5.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.github.serpro69:kotlin-faker:1.6.0")
}


kotlin {
    explicitApi()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"

        if (name == "compileTestKotlin") {
            useIR = true
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
