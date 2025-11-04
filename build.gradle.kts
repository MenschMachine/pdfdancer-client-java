import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

plugins {
    `java-library`
    `maven-publish`
    signing
}

val versionProps = Properties().apply {
    file("version.properties").inputStream().use { load(it) }
}

group = "com.pdfdancer.client"
version = versionProps.getProperty("version") ?: "UNKNOWN"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.17.1")
    implementation("org.slf4j:slf4j-api:2.0.13")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

fun bumpVersion(part: String) {
    val propsFile = file("version.properties")
    val props = Properties()
    propsFile.inputStream().use { props.load(it) }

    val version = props.getProperty("version")
    val (major, minor, patch) = version.split(".").map { it.toInt() }

    val newVersion = when (part) {
        "major" -> "${major + 1}.0.0"
        "minor" -> "$major.${minor + 1}.0"
        "patch" -> "$major.$minor.${patch + 1}"
        else -> version
    }

    props.setProperty("version", newVersion)
    propsFile.outputStream().use { props.store(it, null) }

    println("Bumped version: $newVersion")
}

tasks.register("bumpPatch") { doLast { bumpVersion("patch") } }
tasks.register("bumpMinor") { doLast { bumpVersion("minor") } }
tasks.register("bumpMajor") { doLast { bumpVersion("major") } }
tasks.register("printVersion") {
    doLast {
        println(project.version.toString())
    }
}

tasks.withType<Javadoc> {
    (options as? StandardJavadocDocletOptions)?.apply {
        addBooleanOption("Xdoclint:none", true)
        quiet()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("PDFDancer SDK")
                description.set("Java SDK for PDFDancer API")
                url.set("https://github.com/MenschMachine/pdfdancer-client-java")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("pdfdancer")
                        name.set("PDFDancer.com")
                        email.set("michael.lahr@thefamouscat.com")
                    }
                }
                scm {
                    url.set("https://github.com/MenschMachine/pdfdancer-client-java")
                    connection.set("scm:git:git://github.com/MenschMachine/pdfdancer-client-java.git")
                    developerConnection.set("scm:git:ssh://github.com/MenschMachine/pdfdancer-client-java.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://central.sonatype.com/api/v1/publisher/deploy")
            credentials {
                username = findProperty("sonatypeUsername") as String? ?: System.getenv("SONATYPE_USERNAME")
                password = findProperty("sonatypePassword") as String? ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])

    val keyFilePath = findProperty("signing.keyFile") as String?
    val password = findProperty("signing.password") as String?

    if (!keyFilePath.isNullOrBlank() && !password.isNullOrBlank()) {
        val keyData = Files.readString(Paths.get(keyFilePath))
        useInMemoryPgpKeys(keyData, password)
    } else {
        throw GradleException("Missing signing.keyFile or signing.password property")
    }
}