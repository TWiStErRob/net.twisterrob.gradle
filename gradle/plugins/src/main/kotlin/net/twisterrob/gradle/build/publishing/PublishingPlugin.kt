package net.twisterrob.gradle.build.publishing

import base
import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.component.external.model.TestFixturesSupport
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import publishing
import java
import kotlin

class PublishingPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("org.gradle.maven-publish")
		project.plugins.apply("org.gradle.signing")
		project.plugins.apply("org.jetbrains.dokka")
		setupSources(project)
		setupDoc(project)
		setupSigning(project)
		project.plugins.withId("net.twisterrob.gradle.build.module.library") {
			project.publishing.apply {
				publications {
					create<MavenPublication>("library") library@{
						setupPublication(project)
						// compiled files: artifact(tasks["jar"])) { classifier = null } + dependencies
						from(project.components["java"])
					}
				}
			}
		}
		project.plugins.withId("net.twisterrob.gradle.build.module.gradle-plugin") {
			project.afterEvaluate {
				// Configure built-in pluginMaven publication created by java-gradle-plugin.
				// Have to do it in afterEvaluate, because it's delayed in MavenPluginPublishPlugin.
				project.publishing.apply {
					publications {
						named<MavenPublication>("pluginMaven").configure pluginMaven@{
							setupPublication(project)
							suppressPomMetadataWarningsFor("testFixturesApiElements")
							suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
						}
					}
				}
			}
		}
	}

	companion object {
		const val SOURCES_JAR_TASK_NAME: String = "sourcesJar"
		const val JAVADOC_JAR_TASK_NAME: String = "javadocJar"
		/**
		 * @see org.jetbrains.dokka.gradle.DokkaPlugin
		 */
		const val DOKKA_JAR_TASK_NAME: String = "dokkaJavadoc"
	}
}

private fun MavenPublication.setupPublication(project: Project) {
	project.configure<SigningExtension> {
		sign(this@setupPublication)
	}
	setupModuleIdentity(project)
	setupLinks(project)
	setupArtifacts(project)
	reorderNodes(project)
}

private fun setupDoc(project: Project) {
	val dokkaJavadoc = project.tasks.named<DokkaTask>(PublishingPlugin.DOKKA_JAR_TASK_NAME) {
		// TODO https://github.com/Kotlin/dokka/issues/1894
		moduleName.set(this.project.base.archivesName)
		dokkaSourceSets.configureEach {
			reportUndocumented.set(false)
		}
	}
	val javadocJar = project.tasks.register<Jar>(PublishingPlugin.JAVADOC_JAR_TASK_NAME) {
		archiveClassifier.set("javadoc")
		from(dokkaJavadoc)
	}
	project.artifacts.add("archives", javadocJar)
}

private fun setupSources(project: Project) {
	val sourcesJar = project.tasks.register<Jar>(PublishingPlugin.SOURCES_JAR_TASK_NAME) {
		archiveClassifier.set("sources")
		fun sourcesFrom(sourceSet: SourceSet) {
			from(sourceSet.java.sourceDirectories)
			from(sourceSet.kotlin.sourceDirectories)
			from(sourceSet.resources.sourceDirectories)
		}
		project.plugins.withId("org.gradle.java") {
			sourcesFrom(project.java.sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
		}
		project.plugins.withId("org.gradle.java-test-fixtures") {
			sourcesFrom(project.java.sourceSets[TestFixturesSupport.TEST_FIXTURE_SOURCESET_NAME])
		}
	}
	project.artifacts.add("archives", sourcesJar)
}

private fun setupSigning(project: Project) {
	project.configure<SigningExtension> {
		//val signingKeyId: String? by project // Gradle 6+ only
		// -PsigningKey to gradlew, or ORG_GRADLE_PROJECT_signingKey env var
		val signingKey: String? by project
		// -PsigningPassword to gradlew, or ORG_GRADLE_PROJECT_signingPassword env var
		val signingPassword: String? by project
		if (signingKey != null && signingPassword != null) {
			useInMemoryPgpKeys(signingKey, signingPassword)
		} else {
			setRequired { false }
		}
	}
}

private fun MavenPublication.setupModuleIdentity(project: Project) {
	project.afterEvaluate {
		artifactId = project.base.archivesName.get()
		version = project.version as String

		pom {
			val projectDescription = project.description?.takeIf { it.contains(": ") && it.endsWith(".") }
				?: error(
					"$project must have a description with format: " +
							"\"Module Display Name: Module description.\", " +
							"found ${project.description}"
				)
			name.set(projectDescription.substringBefore(": ").also { check(it.isNotBlank()) })
			description.set(projectDescription.substringAfter(": ").also { check(it.isNotBlank()) })
		}
	}
}

private fun MavenPublication.setupArtifacts(project: Project) {
	artifact(project.tasks.named(PublishingPlugin.SOURCES_JAR_TASK_NAME)) { classifier = "sources" }
	artifact(project.tasks.named(PublishingPlugin.JAVADOC_JAR_TASK_NAME)) { classifier = "javadoc" }
}

private fun MavenPublication.setupLinks(project: Project) {
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

	fun Iterable<*>.nodes(): List<Node> =
		this.filterIsInstance<Node>()

	/**
	 * @see org.gradle.plugins.ear.descriptor.internal.DefaultDeploymentDescriptor.localNameOf
	 */
	fun Node.localName(): String =
		if (this.name() is QName) (this.name() as QName).localPart else this.name().toString()

	fun Node.getChild(localName: String): Node =
		this.getChildren(localName).nodes().singleOrNull()
			?: error("Cannot find $localName in ${this.localName()}: ${this.children().nodes().map { it.localName() }}")

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
