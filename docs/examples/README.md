# Examples

## `release`

Example that uses the publicly available release version of the plugin from `mavenCentral()`.

## `local`

Example that uses the locally published artifacts from `mavenLocal()`. Good for debugging production-like environment
with latest code. To publish before debug use `gradlew publishToMavenLocal`.

## `snapshot`

Example that uses the publicly available snapshot version of the plugin from Sonatype.

## Verify

_Note: local and snapshot has prerequisites that fresh artifacts are available._

```shell
gradlew -Pnet.twisterrob.gradle.build.includeExamples=true assembleExamples checkExamples
```
