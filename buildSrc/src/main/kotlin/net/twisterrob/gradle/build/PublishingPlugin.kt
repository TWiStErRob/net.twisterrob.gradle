package net.twisterrob.gradle.build

import base
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import java
import kotlin

@Suppress("UnstableApiUsage")
class PublishingPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("maven-publish")
		project.plugins.apply("signing")
		project.plugins.apply("org.jetbrains.dokka")
		project.plugins.withId("java-gradle-plugin") {
			project.configure<GradlePluginDevelopmentExtension> {
				// https://github.com/gradle/gradle/issues/11611
				isAutomatedPublishing = false
			}
		}

		val dokkaJavadoc = project.tasks.named<DokkaTask>("dokkaJavadoc") {
			// TODO https://github.com/Kotlin/dokka/issues/1894
			moduleName.set(this.project.base.archivesName)
			dokkaSourceSets.configureEach {
			 	reportUndocumented.set(false)
			}
		}

		val sourcesJar = project.tasks.register<Jar>("sourcesJar") {
			archiveClassifier.set("sources")
			from(project.java.sourceSets["main"].kotlin.sourceDirectories)
		}
		project.artifacts.add("archives", sourcesJar)

		val javadocJar = project.tasks.register<Jar>("javadocJar") {
			archiveClassifier.set("javadoc")
			from(dokkaJavadoc)
		}
		project.artifacts.add("archives", javadocJar)

		project.configure<PublishingExtension> {
			publications {
				register<MavenPublication>("release") {
					setupModuleIdentity(project)
					setupArtifacts(project)
					setupLinks(project)
					reorderNodes(project)
				}
			}
		}
		project.configure<SigningExtension> {
			//val signingKeyId: String? by project // Gradle 6+ only
			// -PsigningKey to gradlew, or ORG_GRADLE_PROJECT_signingKey env var
			val signingKey: String? by project
			// -PsigningPassword to gradlew, or ORG_GRADLE_PROJECT_signingPassword env var
			val signingPassword: String? by project
			if (signingKey != null && signingPassword != null) {
				useInMemoryPgpKeys(signingKey, signingPassword)
				sign(project.the<PublishingExtension>().publications["release"])
			}
		}
	}
}

private fun MavenPublication.setupModuleIdentity(project: Project) {
	project.afterEvaluate {
		artifactId = project.base.archivesName.get()
		version = project.version as String

		pom {
			val projectDescription = project.description?.takeIf { it.contains(':') }
				?: error("$project must have a description with format: \"Module Display Name: Module description.\"")
			@Suppress("UnstableApiUsage")
			name.set(projectDescription.substringBefore(": ").also { check(it.isNotBlank()) })
			@Suppress("UnstableApiUsage")
			description.set(projectDescription.substringAfter(": ").also { check(it.isNotBlank()) })
		}
	}
}

private fun MavenPublication.setupArtifacts(project: Project) {
	if (project.plugins.hasPlugin("com.android.library")) {
		from(project.components["release"])
	} else {
		// compiled files: artifact(tasks["jar"])) { classifier = null } + dependencies
		from(project.components["java"])
		suppressPomMetadataWarningsFor("testFixturesApiElements")
		suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
	}
	artifact(project.tasks.named("sourcesJar")) { classifier = "sources" }
	artifact(project.tasks.named("javadocJar")) { classifier = "javadoc" }
}

private fun MavenPublication.setupLinks(project: Project) {
	@Suppress("UnstableApiUsage")
	pom {
		url.set("https://github.com/TWiStErRob/net.twisterrob.gradle")
		scm {
			connection.set("scm:git:github.com/TWiStErRob/net.twisterrob.gradle.git")
			developerConnection.set("scm:git:ssh://github.com/TWiStErRob/net.twisterrob.gradle.git")
			url.set("https://github.com/TWiStErRob/net.twisterrob.gradle/tree/master")
		}
		licenses {
			license {
				name.set("MIT")
				url.set("https://github.com/TWiStErRob/net.twisterrob.gradle/blob/v${project.version}/LICENCE")
			}
		}
		developers {
			developer {
				id.set("TWiStErRob")
				name.set("Robert Papp")
				email.set("papp.robert.s@gmail.com")
			}
		}
	}
}

private fun MavenPublication.reorderNodes(project: Project) {
	fun Node.getChildren(localName: String): NodeList =
		this.get(localName) as NodeList

	fun NodeList.nodes(): List<Node> =
		(this as Iterable<*>).filterIsInstance<Node>()

	fun Node.getChild(localName: String): Node =
		this.getChildren(localName).nodes().single()

	project.afterEvaluate {
		pom.withXml {
			asNode().apply {
				val lastNodes = listOf(
					getChild("modelVersion"),
					getChild("groupId"),
					getChild("artifactId"),
					getChild("version"),
					getChild("name"),
					getChild("description"),
					getChild("url"),
					getChild("dependencies"),
					getChild("scm"),
					getChild("developers"),
					getChild("licenses")
				)
				lastNodes.forEach { remove(it) }
				lastNodes.forEach { append(it) }
			}
		}
	}
}
