import net.twisterrob.gradle.build.dsl.base
import net.twisterrob.gradle.build.dsl.gradlePlugin
import net.twisterrob.gradle.build.dsl.java
import net.twisterrob.gradle.build.dsl.publishing
import net.twisterrob.gradle.build.publishing.GradlePluginValidationPlugin
import net.twisterrob.gradle.build.publishing.getChild
import net.twisterrob.gradle.build.publishing.withDokkaJar
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.dokka.gradle.DokkaTask
import java.time.Instant
import java.time.format.DateTimeFormatter

plugins {
	id("org.gradle.maven-publish")
	id("org.gradle.signing")
	id("org.jetbrains.dokka")
}
plugins.apply(GradlePluginValidationPlugin::class)

group = project.property("projectGroup").toString()
version = project.property("projectVersion").toString()

plugins.withId("org.gradle.java") {
	afterEvaluate {
		// Delayed configuration, so that project.* is set up properly in corresponding modules' build.gradle.kts.
		tasks.named<Jar>("jar") {
			manifest {
				attributes(
					mapOf(
						// Implementation-* used by TestPlugin
						"Implementation-Vendor" to project.group,
						"Implementation-Title" to project.base.archivesName.get(),
						"Implementation-Version" to project.version,
						"Built-Date" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
					)
				)
			}
		}
	}
}

normalization {
	runtimeClasspath {
		metaInf {
			ignoreAttribute("Built-Date")
		}
	}
}

/**
 * @see org.jetbrains.dokka.gradle.DokkaPlugin
 */
@Suppress("PropertyName", "VariableNaming")
val DOKKA_TASK_NAME: String = "dokkaJavadoc"

// Note: org.gradle.api.publish.plugins.PublishingPlugin.apply calls publications.all,
// so most code here is eagerly executed, even inside register { }!

project.java.withDokkaJar(project, project.tasks.named(DOKKA_TASK_NAME))
project.java.withSourcesJar()
setupDoc(project)
setupSigning(project)
project.plugins.withId("net.twisterrob.gradle.build.module.library") {
	project.publishing.apply {
		publications {
			register<MavenPublication>("library") {
				setupModuleIdentity(project)
				setupPublication(project)
				// compiled files: artifact(tasks["jar"])) { classifier = null } + dependencies
				from(project.components["java"])
			}
		}
	}
}
project.plugins.withId("net.twisterrob.gradle.build.module.gradle-plugin") {
	registerPublicationsTasks(project)
	project.gradlePlugin.apply {
		@Suppress("UnstableApiUsage")
		website = "https://github.com/TWiStErRob/net.twisterrob.gradle"
		@Suppress("UnstableApiUsage")
		vcsUrl = "https://github.com/TWiStErRob/net.twisterrob.gradle.git"
	}
	project.publishing.publications.apply {
		// Pre-configure pluginMaven for MavenPluginPublishPlugin, it'll set up other things.
		create<MavenPublication>("pluginMaven") {
			setupModuleIdentity(project)
			setupPublication(project)
			handleTestFixtures()
		}
		withType<MavenPublication>()
			.matching { it.name.endsWith("PluginMarkerMaven") }
			.configureEach {
				setupPublication(project)
			}
	}
}

fun MavenPublication.setupPublication(project: Project) {
	project.configure<SigningExtension> {
		sign(this@setupPublication)
	}
	setupLinks(project)
	reorderNodes(project)
}

@Suppress("UnusedReceiverParameter")
fun MavenPublication.handleTestFixtures() {
	// Disable publication of test fixtures as it could leak internal dependencies.
	val java = components["java"] as AdhocComponentWithVariants
	java.withVariantsFromConfiguration(configurations.getByName("testFixturesApiElements")) { skip() }
	java.withVariantsFromConfiguration(configurations.getByName("testFixturesRuntimeElements")) { skip() }
	// Not suppressing warnings, because they should be skipped, if they show up, that's a problem.
	//suppressPomMetadataWarningsFor("testFixturesApiElements")
	//suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
}

fun setupDoc(project: Project) {
	project.tasks.named<DokkaTask>(DOKKA_TASK_NAME) {
		// TODO https://github.com/Kotlin/dokka/issues/1894
		moduleName = this.project.base.archivesName
		dokkaSourceSets.configureEach {
			reportUndocumented = false
		}
	}
}

fun setupSigning(project: Project) {
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

fun MavenPublication.setupModuleIdentity(project: Project) {
	// Delayed configuration, so that project.* is set up properly in corresponding modules' build.gradle.kts.
	project.afterEvaluate {
		artifactId = project.base.archivesName.get()
		version = project.version as String

		pom {
			val projectDescription = project.description?.takeIf { it.contains(": ") && it.endsWith(".") }
				?: error(
					"${project} must have a description with format: \"Module Display Name: Module description.\""
							+ ", found ${project.description}"
				)
			name = projectDescription.substringBefore(": ").also { check(it.isNotBlank()) }
			description = projectDescription.substringAfter(": ").also { check(it.isNotBlank()) }
		}
	}
}

fun MavenPublication.setupLinks(project: Project) {
	pom {
		url = "https://github.com/TWiStErRob/net.twisterrob.gradle"
		scm {
			connection = "scm:git:github.com/TWiStErRob/net.twisterrob.gradle.git"
			developerConnection = "scm:git:ssh://github.com/TWiStErRob/net.twisterrob.gradle.git"
			url = "https://github.com/TWiStErRob/net.twisterrob.gradle/tree/main"
		}
		licenses {
			license {
				name = "Unlicense"
				url = "https://github.com/TWiStErRob/net.twisterrob.gradle/blob/v${project.version}/LICENCE"
			}
		}
		developers {
			developer {
				id = "TWiStErRob"
				name = "Robert Papp"
				email = "papp.robert.s@gmail.com"
			}
		}
	}
}

fun MavenPublication.reorderNodes(project: Project) {
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

/**
 * Create convenience lifecycle tasks for markers.
 *
 * @see org.gradle.plugin.devel.plugins.MavenPluginPublishPlugin.createMavenMarkerPublication
 * @see org.gradle.api.publish.maven.plugins.MavenPublishPlugin.createPublishTasksForEachMavenRepo
 */
fun registerPublicationsTasks(project: Project) {
	val markersName = "allPluginMarkerMavenPublications"
	val markersDescription = "all Gradle Plugin Marker publications"
	val markerPublications = project.the<PublishingExtension>()
		.publications
		.matching {
			it is MavenPublication && it.name.endsWith("PluginMarkerMaven")
		}
	project.tasks.register("publish${markersName.capitalized()}ToMavenLocal") task@{
		group = PublishingPlugin.PUBLISH_TASK_GROUP
		description = "Publishes ${markersDescription} produced by this project to the local Maven cache."
		markerPublications.all publication@{
			val publication = this@publication.name
			this@task.dependsOn("publish${publication.capitalized()}PublicationToMavenLocal")
		}
	}
	project.the<PublishingExtension>().repositories.all repository@{
		val repository = this@repository.name
		project.tasks.register("publish${markersName.capitalized()}To${repository.capitalized()}Repository") task@{
			group = PublishingPlugin.PUBLISH_TASK_GROUP
			description = "Publishes ${markersDescription} produced by this project to the ${repository} repository."
			markerPublications.all publication@{
				val publication = this@publication.name
				this@task.dependsOn("publish${publication.capitalized()}PublicationTo${repository.capitalized()}Repository")
			}
		}
	}
}
