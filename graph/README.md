# Gradle task graph visualization

A Gradle Settings Plugin which opens a separate visual window for showing the task graph, updating it in real time as the tasks are executed.

## Usage
`settings.gradle(.kts)`:
```gradle
pluginManagement {
	includeBuild("path/to/this/folder")
}
plugins {
	id("net.twisterrob.graph")
}
```
and execute with `gradlew --no-daemon` (otherwise there are native library classloading problems).
