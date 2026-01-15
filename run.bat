@echo off
set APP_JAR=gui-engine-1.0-SNAPSHOT.jar
set LIB_DIR=lib

java --module-path "%LIB_DIR%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.swing -cp "%LIB_DIR%\*;%APP_JAR%" ru.vsu.cs.cg.App

if errorlevel 1 pause
