@echo off
echo ================================================
echo    Vokabeltrainer wird gestartet...
echo ================================================
echo.

REM SQLite JDBC Treiber herunterladen falls nicht vorhanden
if not exist "sqlite-jdbc.jar" (
    echo SQLite JDBC Treiber wird heruntergeladen...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.0.0/sqlite-jdbc-3.45.0.0.jar' -OutFile 'sqlite-jdbc.jar'"
    echo Download abgeschlossen!
    echo.
)

REM SLF4J Libraries herunterladen falls nicht vorhanden
if not exist "slf4j-api.jar" (
    echo SLF4J API wird heruntergeladen...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar' -OutFile 'slf4j-api.jar'"
)

if not exist "slf4j-simple.jar" (
    echo SLF4J Simple wird heruntergeladen...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar' -OutFile 'slf4j-simple.jar'"
    echo Download abgeschlossen!
    echo.
)

REM Kompilieren
echo Kompiliere Vokabeltrainer.java...
javac -encoding UTF-8 -cp ".;sqlite-jdbc.jar" Vokabeltrainer.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo FEHLER beim Kompilieren!
    pause
    exit /b 1
)

echo Kompilierung erfolgreich!
echo.
echo Starte Vokabeltrainer...
echo.

REM Starten
java -cp ".;sqlite-jdbc.jar;slf4j-api.jar;slf4j-simple.jar" Vokabeltrainer

pause
