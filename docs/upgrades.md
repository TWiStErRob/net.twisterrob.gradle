# How to upgrade AGP in the project?

 1. Update compile AGP version:
    * `libs.versions.toml`: change `agp` and `agp-tools` to the latest stable.
    * Run `gradlew jar` to make things compile.
    * Change `net.twisterrob.test.android.pluginVersion` to the same as `agp`.
 2. Update AGP compat versions
    * Change all `android-gradle-v__x` to the latest patch version.
    * If there's a new alpha/beta/rc/stable release, create a new module: `:compat:agp-__x`.  
      This will be empty, but will allow immediate browsing of source code.
    * Add dependency from `:compat:agp` to the new `:compat:agp-__x`.
    * Add dependency in the new `:compat:agp-__x` to `:compat:agp-base`.
 3. Add compatibility checks via `AGPVersions` if necessary.
    * Review `AGPVersionsTest` to bump latest classpath version.
    * Review `AGPVersionsTest` if there's a new constant.
 4. Review tests
     * `nagging.init.gradle.kts` might need bumping in AGP regexes.
 5. Add new CI matrix based on AGP compatibility guide.
    * These might already exist if you're bumping alpha/beta/rc/stable forward.
    * For example if AGP 7.2.1 requires Gradle 7.3.1 and latest stable is Gradle 7.4.2, create the following combinations:
      ```yaml
      - name: "AGP 7.2.x on Gradle 7.3.1+"
      - name: "AGP 7.2.x on Gradle 7.3.1+ - plugin"
      - name: "AGP 7.2.x on Gradle 7.x"
      - name: "AGP 7.2.x on Gradle 7.x - plugin"
      ```
      Comment out the 7.x version if there's no newer stable yet.
    * After pushing CI changes:
       * Add review comment to add new CI jobs to branch protection rules just before merging.
 6. Update `README.md` table and surrounding text.
 7. Add/rename/delete `docs/debug/agpXXX-gradleYYY` folder to match CI combinations.
 8. Review this document if something was missing.


# How to upgrade Gradle in the project?

 1. Update the gradle wrapper:
    * Run `gradlew wrapper --distribution-type=all --gradle-version=...` in all projects (see `wrappers.bat`)
    * Test each with `gradlew build`.
 2. Update `net.twisterrob.gradle.runner.gradleVersion` to the same as `gradle-wrapper.properties`.
 3. Review tests
    * `VersionsTaskTest....(Gradle X latest)` might need updating.
    * `nagging.init.gradle.kts` might need bumping (both in Gradle version and doc URLs).
      * Run `KotlinPluginIntgTest.can compile Kotlin` to fix trivial Kotlin issues.
      * Run `AndroidBuildPluginIntgTest.can override compileSdk (debug)` to fix trivial Android issues.
      * Run `AndroidBuildPluginIntgTest.can disable buildConfig decoration (debug)` to fix more Android/Kotlin issues.
    * `kotlin-plugin_app\build.gradle` might need bumping (both in Gradle version and doc URLs).
 4. Update CI.yml matrix
    * Update all Gradle `\d\.x` versions to the latest stable.
    * Keep all Gradle `\d\.\d\+` version on the latest patch.
    * Uncomment/add new AGP/Gradle combinations for .x versions.
 5. Update the README.md table and above table version range.
 6. Add/rename/delete `docs/debug/agpXXX-gradleYYY` folder to match CI combinations.
 7. Review this document if something was missing.


# How to upgrade other libraries?
(Future)

 1. Let Renovate open a PR.
 2. Wait for CI, which auto-merges.
 3. Check Release notes just in case.
