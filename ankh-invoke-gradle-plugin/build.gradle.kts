plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
    `kotlin-dsl`
}

group = "org.inksnow"
version = "1.0.21-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("ankh-invoke-gradle-plugin") {
            id = "org.inksnow.ankh-invoke-gradle-plugin"
            implementationClass = "org.inksnow.ankhinvoke.gradle.AnkhInjectorPlugin"
        }
    }
}

dependencies {
    implementation("org.inksnow:ankh-invoke-mapping:1.0.21-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}


publishing {
    repositories {
        mavenLocal()
        if(System.getenv("REPOSITORY_ID") != null) {
            maven {
                name = System.getenv("REPOSITORY_ID")
                url = uri(System.getenv("REPOSITORY_URL"))

                credentials {
                    username = System.getenv("REPOSITORY_USERNAME")
                    password = System.getenv("REPOSITORY_PASSWORD")
                }
            }
        }
    }
}