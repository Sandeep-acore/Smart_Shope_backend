@echo off
echo Starting Smart Shop Backend in local mode...
echo.

REM Set the active profile to local
set SPRING_PROFILES_ACTIVE=local

REM Run the application
mvn spring-boot:run

pause 