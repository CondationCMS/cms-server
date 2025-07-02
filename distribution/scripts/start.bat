@echo off
setlocal
set JAVA=%~dp0jre\bin\java.exe
set JAR=%~dp0lib\your-cms.jar

"%JAVA%" -jar "%JAR%" %*