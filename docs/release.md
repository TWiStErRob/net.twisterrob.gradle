## Publish release `x.y`

 1. `git add CHANGELOG.md & git commit -m "Prepare x.y history"`  
    * `CHANGELOG.md`: review bullet points of what changed  
    * `CHANGELOG.md`: add links to PRs/commits for changes  
 1. `git push origin master:master`
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
 1. `git add gradle.properties CHANGELOG.md & git commit -m "Release x.y"`  
    * `gradle.properties`: remove `-SNAPSHOT` suffix  
    * `CHANGELOG.md`: update version end date
 1. `gradlew bintrayUpload -PbintrayApiKey=...`    
    Available from https://bintray.com/profile/edit > _API Key_  
    _If this fails, fix and amend last commit._
 1. `git push origin master:master`
 1. Publish [drafted release](https://github.com/TWiStErRob/net.twisterrob.gradle/releases) on GitHub  
    Note: _this will create a tag on `master`, equivalent to:_
     ```
    git checkout master
    git tag vx.y
    git push origin vx.y # or --tags
 1. Close `x.y` [milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones)
    * "Due date": release date
    * "Description": https://github.com/TWiStErRob/net.twisterrob.gradle/releases/tag/vx.y

## Prepare next release `x.z`

 1. `git add gradle.properties CHANGELOG.md & git commit -m "Pre-Release x.z"`  
    * `gradle.properties`: version number `x.z-SNAPSHOT`
    * `CHANGELOG.md`: add history section:
    ```
    ## x.z *(YYYY-MM-DD --- )*
     * ...
    ```
 1. [Create milestone](https://github.com/TWiStErRob/net.twisterrob.gradle/milestones/new) `vx.z`, if doesn't exist yet.
