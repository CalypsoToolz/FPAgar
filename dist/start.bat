@echo off
:start
echo Starting Ogar Server.
echo.

java -server -Dfile.encoding=UTF-8 -Dlog4j.configuration=file:./config/log4j.xml -Xmx128m -jar server.jar

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Server restarted ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly ...
echo.
:end
echo.
echo Server terminated ...
echo.

pause
