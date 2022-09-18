The projects in this folder are meant to be used as debugging tools.
If a unit tests fails the files from there can be copied over to the corresponding version
and the project can remote-debug itself.

## Usage
 * open the folder in IDEA as a Gradle project
 * attach sources from the corresponding wrapper
 * place breakpoint
 * debug away with `-Dorg.gradle.debug=true`
