@echo off
setlocal
set "DIR=%~dp0"
set "JAVA=%DIR%java\bin\java.exe"
set "UPDATER=%DIR%bin\cms-updater.jar"

"%JAVA%" %JAVA_OPTS% -jar "%UPDATER%" install --home "%DIR%" %*
exit /b %ERRORLEVEL%
