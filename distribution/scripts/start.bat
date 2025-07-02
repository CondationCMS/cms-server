@echo off
setlocal
set JAVA=%~dp0java\bin\java.exe
set JAR=%~dp0cms-server-@CMS_VERSION@.jar

"%JAVA%" -jar "%JAR%" %*