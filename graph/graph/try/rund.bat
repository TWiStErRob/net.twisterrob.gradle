start "DEBUGGING %~dp0" run.bat -Dorg.gradle.debug=true %*

ping 192.0.0.1 -n 1 -w 1000 >NUL
exit 0
