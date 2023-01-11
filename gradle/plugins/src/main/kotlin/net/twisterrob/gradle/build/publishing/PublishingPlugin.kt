package net.twisterrob.gradle.build.publishing

import base
import disableLoggingFor
import gradlePlugin
import groovy.namespace.QName
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import publishing
import java

class PublishingPlugin : Plugin<Project> {

	override fun apply(project: Project) {
		project.plugins.apply("org.gradle.maven-publish")
		project.plugins.apply("org.gradle.signing")
		project.plugins.apply("org.jetbrains.dokka")
		project.plugins.apply(GradlePluginValidationPlugin::class)
		project.java.withDokkaJar(project, project.tasks.named(DOKKA_TASK_NAME))
		setupDoc(project)
		setupSigning(project)
		project.plugins.withId("net.twisterrob.gradle.build.module.library") {
			project.java.withSourcesJar()
			project.publishing.apply {
				publications {
					create<MavenPublication>("library") library@{
						setupPublication(project)
						// compiled files: artifact(tasks["jar"])) { classifier = null } + dependencies
						from(project.components["java"])
					}
				}
			}
			deduplicateJavadocArtifact(project)
		}
		project.plugins.withId("net.twisterrob.gradle.build.module.gradle-plugin") {
			// Silence: "Signing plugin detected. Will automatically sign the published artifacts."
			disableLoggingFor("com.gradle.publish.PublishTask")
			// Implicitly enables: withSourcesJar, withJavadocJar, signing.
			project.plugins.apply("com.gradle.plugin-publish")
			@Suppress("UnstableApiUsage")
			project.gradlePlugin.apply {
				website.set("https://github.com/TWiStErRob/net.twisterrob.gradle")
				vcsUrl.set("https://github.com/TWiStErRob/net.twisterrob.gradle.git")
			}
			project.afterEvaluate {
				// Configure built-in pluginMaven publication created by java-gradle-plugin.
				// Have to do it in afterEvaluate, because it's delayed in MavenPluginPublishPlugin.
				project.publishing.apply {
					publications {
						named<MavenPublication>("pluginMaven").configure pluginMaven@{
							setupPublication(project)
							// > Maven publication 'pluginMaven' pom metadata warnings
							// > (silence with 'suppressPomMetadataWarningsFor(variant)'):
							// > - Variant testFixturesApiElements:
							// >     - Declares capability net.twisterrob.gradle:checkstyle-test-fixtures:0.15-SNAPSHOT which cannot be mapped to Maven
							// > - Variant testFixturesRuntimeElements:
							// >     - Declares capability net.twisterrob.gradle:checkstyle-test-fixtures:0.15-SNAPSHOT which cannot be mapped to Maven
							// > These issues indicate information that is lost in the published 'pom' metadata file,
							// > which may be an issue if the published library is consumed by an old Gradle version or Apache Maven.
							// > The 'module' metadata file, which is used by Gradle 6+ is not affected.
							suppressPomMetadataWarningsFor("testFixturesApiElements")
							suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
						}
					}
				}
			}
			deduplicateJavadocArtifact(project)
		}
	}

	companion object {
		/**
		 * @see org.jetbrains.dokka.gradle.DokkaPlugin
		 */
		const val DOKKA_TASK_NAME: String = "dokkaJavadoc"
	}
}

private fun MavenPublication.setupPublication(project: Project) {
	setupModuleIdentity(project)
	setupLinks(project)
	reorderNodes(project)
}

private fun setupDoc(project: Project) {
	project.tasks.named<DokkaTask>(PublishingPlugin.DOKKA_TASK_NAME) {
		// TODO https://github.com/Kotlin/dokka/issues/1894
		moduleName.set(this.project.base.archivesName)
		dokkaSourceSets.configureEach {
			reportUndocumented.set(false)
		}
	}
}

/**
 * This is necessary, because `com.gradle.plugin-publish` forces sources and javadoc.
 * But Kotlin Dokka generated Javadoc needs to be registered too.
 * See https://github.com/Kotlin/dokka/issues/558#issuecomment-1377983835
 */
private fun deduplicateJavadocArtifact(project: Project) {
	// Need to delay it, otherwise it doesn't run on variants added late.
	project.afterEvaluate {
		val javadoc = project.configurations[JavaPlugin.JAVADOC_ELEMENTS_CONFIGURATION_NAME]
		(project.components["java"] as AdhocComponentWithVariants).withVariantsFromConfiguration(javadoc) {
			val artifacts = this.configurationVariant.artifacts
			artifacts
				.groupBy {
					"${it.type}:${it.name}-${it.classifier}.${it.extension}@${it.date} - ${it.file.canonicalPath}"
				}
				.forEach { (key, duplicates) ->
					duplicates.drop(1).forEach {
						logger.info("Removing duplicate artifact: $key")
						artifacts.remove(it)
					}
				}
		}
	}
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
