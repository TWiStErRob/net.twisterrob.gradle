## Publish release `x.y`

 1. `git add gradle.properties CHANGELOG.md README.md & git commit -m "Release x.y"`
    * `CHANGELOG.md`: review bullet points of what changed
    * `CHANGELOG.md`: add links to PRs/commits for changes
    * `CHANGELOG.md`: remove empty sections
    * `CHANGELOG.md`: update version end date
    * `gradle.properties`: remove `-SNAPSHOT` suffix
      Double-check that version is `x.y`
    * `README.md`: review compatibility table
 1. Follow the steps at [Central Release Process][1].
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
    * `gradlew publishReleasePublicationToSonatypeRepository`  
     _If this fails, fix and amend last commit._
    * Open [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/#stagingRepositories), log in and close staging repository output at console to validate.
 1. Archive and final integration test.
    * Run `p:\repos\release\net.twisterrob.gradle\download-repo.bat`  
      Need to change mvn2get.json's `remote_repo_urls`.
    * Use it in a real project from staging repository:
      ```gradle
      repositories {
          maven {
              name = "Sonatype Staging for net.twisterrob"
              setUrl("https://s01.oss.sonatype.org/service/local/repositories/nettwisterrob-####/content/")
          }
      }
      ```
 1. Repeat previous steps as necessary.
 1. `git push origin master:master` to get the "Release x.y" commit published.
 1. Publish [drafted release](https://github.com/TWiStErRob/net.twisterrob.gradle/releases) on GitHub
 1. Close `x.y` [milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones) as described in [release process][1].
 1. Release staging repository at [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/#stagingRepositories)
 1. Watch [Maven Central](https://repo1.maven.org/maven2/net/twisterrob/gradle/twister-quality/) for the artifact to appear. May take a few minutes.

## Prepare next release `x.z`

 1. `git add gradle.properties CHANGELOG.md docs/examples/*/build.gradle.kts & git commit -m "Pre-Release x.z"`
    * `gradle.properties`: version number `x.z-SNAPSHOT`
    * `docs/examples/local/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/snapshot/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/release/build.gradle.kts`: plugin version `x.y`
    * `CHANGELOG.md`: add history section:
    ```markdown
    ## x.z *(YYYY-MM-DD --- )*

    ### Breaking
     * ...

    ### New
     * ...

    ### Fixes
     * ...

    ### Deprecations

    #### `net.twisterrob.gradle:artifact-name`
     * ...

    ### Internal
     * ...
    ```
 1. Push commit to `origin/master`.
 1. [Create milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones/new) `vx.z`, if doesn't exist yet.

[1]: https://github.com/TWiStErRob/.github/blob/main/RELEASE.md#release-process
