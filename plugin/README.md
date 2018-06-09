## Versions

0.9.1 / 2.3.3.10-19-26.1 Gradle 3.5.1

3.0.1.10-19-26.0 Gradle 4.1-4.2 (Gradle 4.3 deprecated bootClasspath)

Future: 3.1.0.* Gradle 4.3-4.4

3.1.2.14-27-27.0 Gradle 4.3, 4.4, 4.4.1, 4.5, 4.5.1 (works with 4.6+, but warns)

3.1.3.14-27-27.0 Gradle 4.3, 4.4, 4.4.1, 4.5, 4.5.1 (works with 4.6+, but warns)

## Release
### Testing
To run all integration tests and other checks:
```terminal
gradlew check
```
It takes ~5 minutes.

### `-SNAPSHOT` release
```terminal
gradlew upload
```

### production-ready release
with proper version number (strips `-SNAPSHOT`):
```terminal
gradlew upload -Prelease
```
