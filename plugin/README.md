## Release
### Testing
To run all integration tests and other checks:
```terminal
gradlew check
```
It takes ~7 minutes (52 tests at v3.1.4).

### `-SNAPSHOT` release
```terminal
gradlew upload
```

### production-ready release
with proper version number (strips `-SNAPSHOT`):
```terminal
gradlew upload -Prelease
```
