For the full process see [.github/release.md](https://github.com/TWiStErRob/.github/blob/main/RELEASE.md).

## Publish release `x.y`

 1. Make sure local copy is on latest `main`  
    `git fetch -p & git checkout main & git reset --hard origin/main`
    * `gradle.properties`: remove `-SNAPSHOT` suffix
      Double-check that version is `x.y`
    * `README.md`: review compatibility table
    * Label the `Release x.y` PR `a:feature`, `in:meta` and put it in the milestone.
    * Assign drafted issues/PRs against the milestone (`gh issue edit ### --milestone x.y`)
 1. Upload
    * Set up credentials
        * `sonatypeUsername` is the generated user token username of https://central.sonatype.com/
        * `sonatypePassword` is `sonatypeUsername`'s token
        * `signingKey` is GPG private armored key  
          (just the base64 part on one line, skip the last orphan part)
        * `signingPassword` is `signingKey`'s passphrase
      ```shell
      set ORG_GRADLE_PROJECT_sonatypeUsername=...
      set ORG_GRADLE_PROJECT_sonatypePassword=...
      set ORG_GRADLE_PROJECT_signingKey=...
      set ORG_GRADLE_PROJECT_signingPassword=...
      ```
    * Publish files to Sonatype
      ```shell
      # TODEL --no-configuration-cache https://github.com/gradle-nexus/publish-plugin/issues/221
      gradlew --no-configuration-cache publishLibraryPublicationToSonatypeRepository publishPluginMavenPublicationToSonatypeRepository publishAllPluginMarkerMavenPublicationsToSonatypeRepository :closeSonatypeStagingRepository
      ```  
      (Note: all tasks have to be executed at once, otherwise it creates multiple staging repositories.)
      * _If this fails, fix and push to Release PR._
    * Open [Maven Central Repository > Deployments](https://central.sonatype.com/publishing), log in; check output at console to validate.
 1. Archive and final integration test.
    * Run `p:\repos\release\net.twisterrob.gradle\temp`: `kotlin ../download.main.kts`  
    * Use it in a real project from staging repository (update version number!):
      ```kotlin
      repositories {
          exclusiveContent {
              forRepository {
                  maven("https://central.sonatype.com/api/v1/publisher/deployments/download/") {
                      name = "Maven Central Unpublished"
                      credentials(HttpHeaderCredentials::class) {
                          name = "Authorization"
                          value = "Bearer ${System.getenv("SONATYPE_TOKEN")}"
                      }
                      authentication {
                          create<HttpHeaderAuthentication>("header")
                      }
                  }
              }
              filter {
                  includeVersionByRegex("""^net\.twisterrob\.gradle$""", ".*", "^${Regex.escape("x.y")}$")
                  includeVersionByRegex("""^net\.twisterrob\.gradle\.plugin\.[^.]+$""", ".*", "^${Regex.escape("x.y")}$")
              }
          }
      }
      ```
 1. Repeat previous steps as necessary.
 1. Merge the Release PR to `main`.
 1. Publish deployment at [Maven Central Repository > Deployments](https://central.sonatype.com/publishing)
 1. Watch [Maven Central](https://repo1.maven.org/maven2/net/twisterrob/gradle/twister-quality/) for the artifact to appear. May take a few minutes.

## Prepare next release `x.z`
 1. Create a PR titled "Pre-Release x.z" to `main` with the following changes:
    * `gradle.properties`: version number `x.z-SNAPSHOT`
    * `docs/examples/local/build.gradle.kts` + `settings.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/snapshot/build.gradle.kts` + `settings.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/release/build.gradle.kts` + `settings.gradle.kts`: plugin version `x.y`
    * `sample/sample/settings.gradle`: plugin version `x.y`
    * Label the PR `a:feature`, `in:meta`.
 1. [Create milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones/new) `vx.z`, if doesn't exist yet.

[1]: https://github.com/TWiStErRob/.github/blob/main/RELEASE.md#release-process

## Diagnostics

List all repositories
```shell
curl -u "%ORG_GRADLE_PROJECT_sonatypeUsername%:%ORG_GRADLE_PROJECT_sonatypePassword%" "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any&profile_id=net.twisterrob" | jq
```

Drop a repository
```shell
curl -X DELETE -u "%ORG_GRADLE_PROJECT_sonatypeUsername%:%ORG_GRADLE_PROJECT_sonatypePassword%" "https://ossrh-staging-api.central.sonatype.com/manual/drop/repository/<ID from list>"
```
