@echo off

set /p user_port=Enter port number:

if "%JAVA_HOME%" == "" goto setJavaHome

:setJavaHome
set /p jdk_path=Enter JDK path: 
set JAVA_HOME=%jdk_path%
goto init

:init
if exist "%JAVA_HOME%\bin\java.exe" goto start

echo Error: JAVA_HOME is set to an invalid directory.

:start
%JAVA_HOME%\bin\java -jar target\round-robin-api-app.jar --server.port=%user_port%

set /p dummy_var=Press any key to exit