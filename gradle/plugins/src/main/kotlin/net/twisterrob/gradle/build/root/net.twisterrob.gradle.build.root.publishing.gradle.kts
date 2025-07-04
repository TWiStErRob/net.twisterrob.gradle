plugins {
	id("io.github.gradle-nexus.publish-plugin")
}

nexusPublishing {
	val projectGroup = project.providers.gradleProperty("projectGroup")
	val projectVersion = project.providers.gradleProperty("projectVersion")
	packageGroup = projectGroup
	useStaging = projectVersion.map { !it.endsWith("-SNAPSHOT") }
	repositoryDescription = project.provider { "${projectGroup.get()}:${projectVersion.get()}" }
	repositories {
		sonatype {
			// For :publish...PublicationToSonatypeRepository, projectVersion suffix chooses repo.
			nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
			snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")

			// For :closeAndReleaseSonatypeStagingRepository
			// Set via -PsonatypeStagingProfileId to gradlew, or ORG_GRADLE_PROJECT_sonatypeStagingProfileId env var.
			val sonatypeStagingProfileId: String? by project
			stagingProfileId = sonatypeStagingProfileId

			val sonatypeUsername: String? by project
			val sonatypePassword: String? by project
			@Suppress("UnnecessaryParentheses") // REPORT Would be confusing without.
			require((sonatypeUsername == null) == (sonatypePassword == null)) {
				// Explicit check for existence of both, because otherwise it just fails with a misleading error:
				// > Execution failed for task ':initializeSonatypeStagingRepository'.
				// > > Failed to load staging profiles, server at ${nexusUrl} responded with status code 401, body:
				"Missing username (${sonatypeUsername == null}) or password (${sonatypePassword == null})."
			}
			// For everything sonatype, but automatically done by the plugin.
			// Set via -PsonatypeUsername to gradlew, or ORG_GRADLE_PROJECT_sonatypeUsername env var.
			//username = sonatypeUsername
			// Set via -PsonatypePassword to gradlew, or ORG_GRADLE_PROJECT_sonatypePassword env var.
			//password = sonatypePassword
		}
	}
}
