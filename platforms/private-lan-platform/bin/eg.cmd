@echo off

rem This script provides the command and control utility for starting
rem Rio services and the Rio command line interface

rem Use local variables
setlocal

rem Set local variables
if "%EG_HOME%" == "" set EG_HOME=%~dp0..
set RIO_HOME=%EG_HOME%
if "%JINI_HOME%" == "" goto noJiniHome
goto haveJiniHome

:noJiniHome
if not exist "%RIO_HOME%\lib\apache-river" goto jiniNotFound
set JINI_HOME=%RIO_HOME%\lib\apache-river
goto haveJiniHome

:jiniNotFound
echo Cannot locate expected Jini (River) distribution, either set JINI_HOME or download Rio with dependencies, exiting
goto exitWithError

:haveJiniHome
set JINI_LIB=%JINI_HOME%\lib

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
goto endOfJavaHome

:noJavaHome
set JAVACMD=java.exe
:endOfJavaHome  

if "%JAVA_MEM_OPTIONS%" == "" set JAVA_MEM_OPTIONS=-Xms256m -Xmx256m

rem Parse command line
if "%1"=="" goto interactive
if "%1"=="start" goto start

:interactive
rem set cliExt=%RIO_HOME%\config\rio_cli.config
rem set cliExt=""
set command_line=%*
set launchTarget=org.rioproject.tools.cli.CLI
set classpath=-cp %RIO_HOME%\lib\rio-cli.jar;%JINI_LIB%\jsk-lib.jar;%JINI_LIB%\jsk-platform.jar;%RIO_HOME%\lib\spring\spring.jar;%RIO_HOME%\lib\jakarta-commons\commons-logging.jar;%RIO_HOME%\lib\groovy\groovy-all-1.6-beta-2.jar
set props="-DRIO_HOME=%RIO_HOME% -DJINI_HOME=%JINI_HOME%"
"%JAVACMD%" %classpath% -Xms256m -Xmx256m ^
    -Djava.util.logging.config.file=%RIO_HOME%/config/rio-cli.logging.properties ^
    -DRIO_HOME=%RIO_HOME% -DJINI_HOME=%JINI_HOME% -Djava.security.policy=%RIO_HOME%\policy\policy.all ^
    %launchTarget% %cliExt% %command_line%
goto end

:start

rem Get the service starter
shift
if "%1"=="" goto noService
set starterConfig=%RIO_HOME%\config\start-%1.config
if not exist "%starterConfig%" goto noStarter
shift

echo "starter config [%starterConfig%]"
set RIO_LOG_DIR="%RIO_HOME%"\logs\
set RIO_NATIVE_DIR="%RIO_HOME%"\lib\native;"%RIO_HOME%"\lib\hyperic
set PATH=%PATH%;"%RIO_NATIVE_DIR%

set classpath=-cp %RIO_HOME%\lib\boot.jar;%JINI_HOME%\lib\start.jar;%EG_HOME%\lib\elastic-grid\kernel\elastic-grid-core-${pom.version}.jar
set agentpath=-javaagent:%RIO_HOME%\lib\boot.jar

set launchTarget=com.sun.jini.start.ServiceStarter

"%JAVA_HOME%\bin\java" -server %JAVA_MEM_OPTIONS% %classpath% %agentpath% ^
    -Djava.security.policy=%RIO_HOME%\policy\policy.all ^
    -Djava.protocol.handler.pkgs=net.jini.url ^
    -Djava.library.path=%RIO_NATIVE_DIR% ^
    -DJINI_HOME=%JINI_HOME% ^
    -DRIO_HOME=%EG_HOME% ^
    -DEG_HOME=%EG_HOME% ^
    -Dorg.rioproject.home=%RIO_HOME% ^
    -DRIO_NATIVE_DIR=%RIO_NATIVE_DIR% ^
    -DRIO_LOG_DIR=%RIO_LOG_DIR% ^
    %launchTarget% ^
    %starterConfig%
goto end

:noStarter
echo Cannot locate expected service starter file [start-%1.config] in [%RIO_HOME%\config], exiting"
goto exitWithError

:noService
echo "A service to start is required, exiting"

:exitWithError
exit /B 1

:end
endlocal
title Command Prompt
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
exit /B 0