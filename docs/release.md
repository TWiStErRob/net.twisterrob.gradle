For the full process see [.github/release.md](https://github.com/TWiStErRob/.github/blob/main/RELEASE.md).

## Publish release `x.y`

 1. Make sure local copy is on latest `master`  
    `git fetch -p & git checkout master & git reset --hard origin/master`
 1. `git add gradle.properties README.md & git commit -m "Release x.y"`
    * `gradle.properties`: remove `-SNAPSHOT` suffix
      Double-check that version is `x.y`
    * `README.md`: review compatibility table
 1. Upload
    * Set up credentials
        * `sonatypeUsername` is the account name of https://s01.oss.sonatype.org/
        * `sonatypePassword` is `sonatypeUsername`'s password
        * `signingKey` is GPG private armored key  
          (just the base64 part on one line, skip the last orphan part)
        * `signingPassword` is `signingKey`'s passphrase
        ```shell
        set ORG_GRADLE_PROJECT_sonatypeUsername=...
        set ORG_GRADLE_PROJECT_sonatypePassword=...
        set ORG_GRADLE_PROJECT_signingKey=...
        set ORG_GRADLE_PROJECT_signingPassword=...
        ```
    * ```shell
      gradlew publishLibraryPublicationToSonatypeRepository
      gradlew publishPluginMavenPublicationToSonatypeRepository
      gradlew publishAllPluginMarkerMavenPublicationsToSonatypeRepository
      ```  
     _If this fails, fix and amend last commit._
    * Open [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/#stagingRepositories), log in and close staging repository output at console to validate.
 1. Archive and final integration test.
    * Run `p:\repos\release\net.twisterrob.gradle\download-repo.bat`  
      Need to change mvn2get.json's `remote_repo_urls`.
    * Use it in a real project from staging repository:  
      Place it first to make sure Gradle resolves it, and update URL and version number exactly.
      ```gradle
      repositories {
          maven("https://s01.oss.sonatype.org/service/local/repositories/nettwisterrob-####/content/") {
              name = "Sonatype Staging for net.twisterrob"
              content {
                  includeVersionByRegex("""^net\.twisterrob\.gradle$""", ".*", "^${Regex.escape("x.y")}$")
              }
          }
      }
      ```
 1. Repeat previous steps as necessary.
 1. `git push origin master:master` to get the "Release x.y" commit published.
 1. Release staging repository at [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/#stagingRepositories)
 1. Watch [Maven Central](https://repo1.maven.org/maven2/net/twisterrob/gradle/twister-quality/) for the artifact to appear. May take a few minutes.

## Prepare next release `x.z`

 1. `git add gradle.properties docs/examples/*/build.gradle.kts & git commit -m "Pre-Release x.z"`
    * `gradle.properties`: version number `x.z-SNAPSHOT`
    * `docs/examples/local/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/snapshot/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/release/build.gradle.kts`: plugin version `x.y`
 1. Push commit to `origin/master`.
 1. [Create milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones/new) `vx.z`, if doesn't exist yet.

[1]: https://github.com/TWiStErRob/.github/blob/main/RELEASE.md#release-process
