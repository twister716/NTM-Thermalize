@echo off
rem Golem harness CLI wrapper (Windows cmd / PowerShell). Downloads the CLI of the Golem Core release named by
rem deps.golemcore in gradle.properties on first use, then runs it on a Java 21+ runtime.
rem Prefer the POSIX wrapper from Git Bash: cmd mangles quotes in JSON arguments (`exec`, `query`).
setlocal
for %%I in ("%~dp0..") do set "ROOT=%%~fI"
for /f "tokens=1,* delims==" %%A in ('findstr /b /l "deps.golemcore=" "%ROOT%\gradle.properties"') do set "VERSION=%%B"
set "JAR=%ROOT%\.harness\golem-harness-%VERSION%.jar"
if not exist "%JAR%" (
    echo [harness] downloading the harness CLI of Golem Core %VERSION%...
    if not exist "%ROOT%\.harness" mkdir "%ROOT%\.harness"
    curl -fsSL -o "%JAR%.part" "https://github.com/Warfactory-Official/Golem-Core/releases/download/v%VERSION%/golem-harness.jar" || (del "%JAR%.part" 2>nul & exit /b 1)
    move /y "%JAR%.part" "%JAR%" >nul
)
set "JAVA_EXE=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
for /d %%D in ("%USERPROFILE%\.gradle\jdks\*21*") do if exist "%%~D\bin\java.exe" set "JAVA_EXE=%%~D\bin\java.exe"
set "HARNESS_CONFIG=%ROOT%\harness.json"
"%JAVA_EXE%" -jar "%JAR%" %*
exit /b %ERRORLEVEL%
