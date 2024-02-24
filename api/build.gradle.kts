val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
	application
	kotlin("jvm") version "1.9.22"
	id("io.ktor.plugin") version "2.3.8"
	id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

group = "fr.imacaron.gif"
version = "0.0.1"

application {
	mainClass.set("io.ktor.server.cio.EngineMain")

	val isDevelopment: Boolean = project.ext.has("development")
	applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.ktor:ktor-server-core-jvm")
	implementation("io.ktor:ktor-server-resources")
	implementation("io.ktor:ktor-server-host-common-jvm")
	implementation("io.ktor:ktor-server-status-pages-jvm")
	implementation("io.ktor:ktor-server-cors-jvm")
	implementation("io.ktor:ktor-server-forwarded-header-jvm")
	implementation("io.ktor:ktor-server-openapi")
	implementation("io.ktor:ktor-server-swagger-jvm")
	implementation("io.ktor:ktor-server-call-logging-jvm")
	implementation("io.ktor:ktor-server-content-negotiation-jvm")
	implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
	implementation("io.ktor:ktor-server-cio-jvm")
	implementation("ch.qos.logback:logback-classic:$logback_version")
	implementation("io.ktor:ktor-server-config-yaml:2.3.8")
	testImplementation("io.ktor:ktor-server-tests-jvm")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

tasks {
	val fatJar = register<Jar>("fatJar") {
		dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
		archiveClassifier.set("standalone") // Naming the jar
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
		exclude("META-INF/*.RSA")
		exclude("META-INF/*.SF")
		exclude("META-INF/*.DSA")
		val sourcesMain = sourceSets.main.get()
		val contents = configurations.runtimeClasspath.get()
			.map { if (it.isDirectory) it else zipTree(it) } +
				sourcesMain.output
		from(contents)
		archiveFileName = "gif-api.jar"
	}
	build {
		dependsOn(fatJar) // Trigger fat jar creation during build
	}
}

kotlin {
	jvmToolchain(17)
}