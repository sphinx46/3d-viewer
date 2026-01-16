@echo off
setlocal enabledelayedexpansion

set APP_JAR=gui-engine-1.0-SNAPSHOT.jar
set LIB_DIR=lib

echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH!
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo Checking files...
if not exist "%APP_JAR%" (
    echo ERROR: Application JAR file not found: %APP_JAR%
    pause
    exit /b 1
)

if not exist "%LIB_DIR%" (
    echo ERROR: Library directory not found: %LIB_DIR%
    pause
    exit /b 1
)

echo Checking JavaFX libraries...
set MISSING=0
for %%m in (javafx-base javafx-controls javafx-graphics javafx-fxml) do (
    if not exist "%LIB_DIR%\%%m.jar" (
        echo ERROR: Missing required library: %%m.jar
        set MISSING=1
    )
)

if %MISSING%==1 (
    echo.
    echo Please download JavaFX from:
    echo https://gluonhq.com/products/javafx/
    echo and extract all JAR files to the %LIB_DIR% folder
    pause
    exit /b 1
)

echo Starting application...

REM Вариант 1: Используем папку lib для module-path (самый простой способ)
java --module-path "%LIB_DIR%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.swing ^
     -cp "%APP_JAR%;%LIB_DIR%\*" ^
     ru.vsu.cs.cg.App

if errorlevel 1 (
    echo.
    echo Application failed to start with error code: %errorlevel%
    echo.
    echo Troubleshooting steps:
    echo 1. Make sure Java 11+ is installed
    echo 2. Check that all JavaFX JAR files are in the %LIB_DIR% folder
    echo 3. Verify the file structure:
    echo    %CD%\
    echo    ├── %APP_JAR%
    echo    ├── run.bat
    echo    └── %LIB_DIR%\
    echo        ├── javafx-base.jar
    echo        ├── javafx-controls.jar
    echo        ├── javafx-fxml.jar
    echo        ├── javafx-graphics.jar
    echo        └── javafx-swing.jar
    pause
)