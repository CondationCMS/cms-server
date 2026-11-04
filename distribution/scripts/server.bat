@echo off
setlocal
set JAVA=%~dp0java\bin\java.exe
set JAR=%~dp0cms-server-@CMS_VERSION@.jar

if not defined JAVA_OPTS (
    set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseZGC
)
set JAVA_OPTS=%JAVA_OPTS% --enable-native-access=ALL-UNNAMED  -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=true

"%JAVA%" %JAVA_OPTS% -jar "%JAR%" %*

endlocal