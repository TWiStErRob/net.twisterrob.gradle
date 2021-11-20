call gradlew.bat --stop
start "DEBUGGING %~dp0" gradlew.bat --no-daemon -Dorg.gradle.debug=true %*

ping 192.0.0.1 -n 1 -w 1000 >NUL
exit 0
