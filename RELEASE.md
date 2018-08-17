## Publish release

 1. `git add CHANGELOG.md & git commit -m "Prepare x.y history"`  
    (bullet points, optional if PRs/commits maintained nicely)
 1. `git add CHANGELOG.md & git commit -m "Release x.y"`  
    update version end date
 1. `git push origin master:master`
 1. `git tag vx.y`
 1. `git push origin vx.y` (or `--tags`)
 1. `gradlew bintray -PbintrayApiKey=...`
 1. Draft and publish a new release on GitHub (update links to latest!):
    ```
    * important change 1 from change log
    * important change 2 from change log
    
    For full list of changes see [change log][1], [release milestone][2] and [diff][3].

    [1]: https://github.com/TWiStErRob/net.twisterrob.gradle/blob/v0.5/CHANGELOG.md#05-2018-04-02-----2018-04-16
    [2]: https://github.com/TWiStErRob/net.twisterrob.gradle/milestone/1?closed=1
    [3]: https://github.com/TWiStErRob/net.twisterrob.gradle/compare/v0.4...v0.5


## Prepare next release

 1. `git add gradle.properties CHANGELOG.md & git commit -m "Pre-Release x.y+1"`  
    update snapshot version number and add history section
