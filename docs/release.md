## Publish release `x.y`

 1. `git add gradle.properties CHANGELOG.md & git commit -m "Release x.y"`
    * `CHANGELOG.md`: review bullet points of what changed
    * `CHANGELOG.md`: add links to PRs/commits for changes
    * `CHANGELOG.md`: update version end date
    * `gradle.properties`: remove `-SNAPSHOT` suffix
      Double-check that version is `x.y`
 1. [Draft a new release](https://github.com/TWiStErRob/net.twisterrob.gradle/releases/new) on GitHub
    * "_Tag version_": `vx.y` @ Target: `master`
    * "_Release title_": `x.y Two Word Summary` (e.g. biggest change / reason for release)
    * "_Describe this release_":
    ```
    * TODO important change 1 from change log
    * TODO important change 2 from change log
    
    For full list of changes see [change log][1], [release milestone][2] and [diff][3].

    <!-- TODO update these links to latest -->
    [1]: https://github.com/TWiStErRob/net.twisterrob.gradle/blob/v0.5/CHANGELOG.md#05-2018-04-02-----2018-04-16
    [2]: https://github.com/TWiStErRob/net.twisterrob.gradle/milestone/1?closed=1
    [3]: https://github.com/TWiStErRob/net.twisterrob.gradle/compare/v0.4...v0.5
    ```
 1. Upload
    * Set up credentials
        * `sonatypeUsername` is the account name of https://s01.oss.sonatype.org/
        * `sonatypePassword` is `sonatypeUsername`'s password
        * `signingKey` is GPG private armored key (just the base64 part on one line)
        * `signingPassword` is `signingKey`'s passphrase
        ```bash
        set ORG_GRADLE_PROJECT_sonatypeUsername=...
        set ORG_GRADLE_PROJECT_sonatypePassword=...
        set ORG_GRADLE_PROJECT_signingKey=...
        set ORG_GRADLE_PROJECT_signingPassword=...
        ```
    * `gradlew publishReleasePublicationToSonatypeRepository`  
     _If this fails, fix and amend last commit._
    * Open URL, log in and close staging repository to validate.
 1. Archive and final integration test.
    * Run `p:\repos\release\net.twisterrob.gradle\download-repo.bat`
    * Use it in a real project from staging repository:
      ```
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
    Note: _this will create a tag on `master`, equivalent to:_
     ```
    git checkout master
    git tag vx.y
    git push origin vx.y # or --tags
 1. Close `x.y` [milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones)
    * "Due date": release date
    * "Description": https://github.com/TWiStErRob/net.twisterrob.gradle/releases/tag/vx.y
 1. Release staging repository at [Sonatype Nexus Repository Manager](https://s01.oss.sonatype.org/#stagingRepositories)

## Prepare next release `x.z`

 1. `git add gradle.properties CHANGELOG.md docs/examples/*/build.gradle.kts & git commit -m "Pre-Release x.z"`
    * `gradle.properties`: version number `x.z-SNAPSHOT`
    * `docs/examples/local/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/snapshot/build.gradle.kts`: plugin version `x.z-SNAPSHOT`
    * `docs/examples/release/build.gradle.kts`: plugin version `x.y`
    * `CHANGELOG.md`: add history section:
    ```
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
 1. [Create milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones/new) `vx.z`, if doesn't exist yet.
