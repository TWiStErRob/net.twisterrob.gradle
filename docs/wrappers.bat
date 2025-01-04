set version=8.11.1
call gradlew wrapper --distribution-type=all --gradle-version=%version%
call gradlew -p gradle/plugins wrapper --distribution-type=all --gradle-version=%version%
call gradlew -p graph wrapper --distribution-type=all --gradle-version=%version%
call gradlew -p docs/examples/snapshot wrapper --distribution-type=all --gradle-version=%version%
call gradlew -p docs/examples/release wrapper --distribution-type=all --gradle-version=%version%
call gradlew -p docs/examples/local wrapper --distribution-type=all --gradle-version=%version%
