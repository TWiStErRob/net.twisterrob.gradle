## Gradle Plugins
```gradle
apply plugin: net.twisterrob.<...>
```

### net.twisterrob.android-app
Replacement for `com.android.application`.

### net.twisterrob.android-library
Replacement for `com.android.library`.

### net.twisterrob.android-test
Replacement for `com.android.test`.

### net.twisterrob.java
Replacement for `java`.

### net.twisterrob.java-library
Replacement for `java-library`.

### net.twisterrob.kotlin
Replacement for `kotlin`, `kotlin-android`, `kotlin-kapt`.

### net.twisterrob.root
#### `:gradleWrapper`
Generates a `gradled.bat` which can be used instead of `gradlew.bat` to start Gradle in Debug mode.
It will make sure there's only one instance of Gradle is running, display a console window and wait for the debugger to attach before starting.

### net.twisterrob.vcs
Provides DSL access to GIT and SVN revision info:
```
project.VCS.current.revision // String
project.VCS.current.revisionNumber // int
project.VCS.current.isAvailable // boolean
project.VCS.current.isAvailableQuick // boolean
```
