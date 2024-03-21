# Gradle task graph visualization

A Gradle Settings Plugin which opens a separate visual window for showing the task graph, updating it in real time as the tasks are executed.

## Usage
`settings.gradle(.kts)`:
```gradle
pluginManagement {
	includeBuild("path/to/this/folder")
}
plugins {
	id("net.twisterrob.gradle.graph")
}
```
and execute with `gradlew --no-daemon` (otherwise there are native library classloading problems).

## Testing
Run `run.bat` in the `sample` folder for a visual integration test.

## Development - d3

Run `gradlew build` to generate the `d3.js` file.
Open `graph/src/main/resources/d3-graph.html` in a browser to see a demo graph.
Edit and refresh as normal.
