@echo off
setlocal
set JAVA=%~dp0java\bin\java.exe
set JAR=%~dp0cms-server-@CMS_VERSION@.jar

if not defined JAVA_OPTS (
    set JAVA_OPTS=-Xms256m -Xmx512m
)

"%JAVA%" %JAVA_OPTS% -jar "%JAR%" %*