import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("pdfdancer-client.properties") {
        expand("version" to project.version)
    }
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
                name.set("PDFDancer Java Client")
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
            name = "CentralPortal"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")

            credentials {
                username = findProperty("centralPortalUsername") as String? ?: System.getenv("CENTRAL_PORTAL_USERNAME")
                password = findProperty("centralPortalPassword") as String? ?: System.getenv("CENTRAL_PORTAL_PASSWORD")
            }
        }
    }
}

// Signing configuration - only required for publishing to Maven Central
val keyFilePath = findProperty("signing.keyFile") as String?
val password = findProperty("signing.password") as String?
val signingEnabled = !keyFilePath.isNullOrBlank() && !password.isNullOrBlank()

if (signingEnabled) {
    signing {
        val keyData = Files.readString(Paths.get(keyFilePath))
        useInMemoryPgpKeys(keyData, password)
        sign(publishing.publications["mavenJava"])
    }
} else {
    // Signing is disabled - builds will succeed but publishing to Maven Central will fail
    logger.warn("Signing is disabled. Set signing.keyFile and signing.password to enable artifact signing.")
}

tasks.register("mavenCentralBundle") {
    group = "publishing"
    description = "Creates a bundle.zip suitable for uploading to Maven Central (requires signing credentials)"

    dependsOn("publishToMavenLocal")

    val bundleFile = layout.buildDirectory.file("distributions/bundle.zip")
    outputs.file(bundleFile)

    doFirst {
        // Check if signing is enabled
        val keyFilePath = findProperty("signing.keyFile") as String?
        val password = findProperty("signing.password") as String?
        if (keyFilePath.isNullOrBlank() || password.isNullOrBlank()) {
            throw GradleException(
                "Maven Central bundle requires signing credentials.\n" +
                "Please set signing.keyFile and signing.password properties."
            )
        }
    }

    doLast {
        val publication = publishing.publications["mavenJava"] as MavenPublication
        val artifactId = publication.artifactId
        val version = project.version.toString()
        val groupPath = project.group.toString().replace('.', '/')

        val localRepoPath = file("${System.getProperty("user.home")}/.m2/repository/$groupPath/$artifactId/$version")

        if (!localRepoPath.exists()) {
            throw GradleException("Local Maven repository artifacts not found at: $localRepoPath")
        }

        // Generate MD5 and SHA1 checksums for all artifacts
        val artifactFiles = fileTree(localRepoPath).matching {
            include("*.jar", "*.pom", "*.module")
        }.files

        fun generateChecksum(file: File, algorithm: String): String {
            val digest = MessageDigest.getInstance(algorithm)
            val bytes = file.readBytes()
            val hash = digest.digest(bytes)
            return hash.joinToString("") { "%02x".format(it) }
        }

        artifactFiles.forEach { file ->
            // Generate MD5
            val md5 = generateChecksum(file, "MD5")
            File(localRepoPath, "${file.name}.md5").writeText(md5)

            // Generate SHA1
            val sha1 = generateChecksum(file, "SHA-1")
            File(localRepoPath, "${file.name}.sha1").writeText(sha1)
        }

        // Create the zip file with proper directory structure
        ant.withGroovyBuilder {
            "zip"("destfile" to bundleFile.get().asFile.absolutePath) {
                "zipfileset"("dir" to localRepoPath.absolutePath, "prefix" to "$groupPath/$artifactId/$version") {
                    "include"("name" to "*.jar")
                    "include"("name" to "*.pom")
                    "include"("name" to "*.module")
                    "include"("name" to "*.asc")
                    "include"("name" to "*.md5")
                    "include"("name" to "*.sha1")
                }
            }
        }

        val allFiles = fileTree(localRepoPath).matching {
            include("*.jar", "*.pom", "*.module", "*.asc", "*.md5", "*.sha1")
        }.files.sortedBy { it.name }

        println("\nMaven Central bundle created successfully!")
        println("Location: ${bundleFile.get().asFile}")
        println("\nBundle contains ${allFiles.size} files:")
        allFiles.forEach { println("  ✓ ${it.name}") }
        println("\nYou can now upload this bundle to Maven Central.")
    }
}

tasks.register("printBundleInfo") {
    group = "publishing"
    description = "Prints information about the Maven Central bundle"

    doLast {
        val publication = publishing.publications["mavenJava"] as MavenPublication
        val artifactId = publication.artifactId
        val version = project.version.toString()

        println("""
            |
            |Maven Central Bundle Information:
            |=================================
            |Group ID:    ${project.group}
            |Artifact ID: $artifactId
            |Version:     $version
            |
            |To create the bundle:
            |  ./gradlew mavenCentralBundle
            |
            |The bundle will include:
            |  - ${artifactId}-${version}.jar
            |  - ${artifactId}-${version}.jar.asc
            |  - ${artifactId}-${version}-sources.jar
            |  - ${artifactId}-${version}-sources.jar.asc
            |  - ${artifactId}-${version}-javadoc.jar
            |  - ${artifactId}-${version}-javadoc.jar.asc
            |  - ${artifactId}-${version}.pom
            |  - ${artifactId}-${version}.pom.asc
            |
            |Output location:
            |  build/distributions/bundle.zip
            |
        """.trimMargin())
    }
}