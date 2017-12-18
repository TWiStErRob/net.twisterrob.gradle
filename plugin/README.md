## Versions

0.9.1 / 2.3.3.10-19-26.1 Gradle 3.5.1

3.0.1.10-19-26.0 Gradle 4.1-4.2 (Gradle 4.3 deprecated bootClasspath)

Future: 3.1.0.* Gradle 4.3-4.4


## Release
`-SNAPSHOT` release
```terminal
gradlew upload
```

production-ready release with proper version number (strips `-SNAPSHOT`):
```terminal
gradlew upload -Prelease
```
