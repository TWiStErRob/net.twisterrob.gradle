# Gradle Quality plugins

```gradle
apply plugin: 'net.twisterrob:quality'
```
above includes (but you can cherry-pick them):
```gradle
apply plugin: 'net.twisterrob:checkstyle'
```


## Versions

### 0.1: 2018-01-05
Android Gradle Plugin 3.0.1, Gradle: 4.2.1


## Development

To run tests from Android Studio, run it as usual, but edit the "Gradle-aware Make" to run {@code classes testClasses} tasks.

To run this test from IntelliJ IDEA, run it as usual, but first set: *Build, Execution, Deployment > Build Tools > Gradle > Runner > Run tests using:* in Settings to **Gradle Test Runner**
