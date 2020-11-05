## Release
### Testing
To run all integration tests and other checks:
```terminal
gradlew check
```
It takes ~7 minutes (52 tests at v3.1.4) on laptop.
It takes ~1.5 minutes (59 tests at v3.4.2) on 10 parallel threads on PC.

### `-SNAPSHOT` release
```terminal
gradlew publish
```

### production-ready release
with proper version number (strips `-SNAPSHOT`):
```terminal
gradlew publish -Prelease
```
