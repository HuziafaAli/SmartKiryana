@echo off
echo Cleaning bin directory...
if exist bin rmdir /s /q bin
mkdir bin

echo Finding Java sources...
dir /s /b src\*.java > sources.txt

echo Compiling Java sources...
javac -d bin -cp "lib\postgresql-42.7.3.jar" --module-path "lib\javafx-sdk-17.0.19\lib" --add-modules javafx.controls,javafx.fxml @sources.txt

echo Copying resources (FXML, CSS, Assets)...
if not exist bin\ui mkdir bin\ui
if not exist bin\ui\assets mkdir bin\ui\assets

xcopy /s /y src\ui\*.fxml bin\ui\ > nul
xcopy /s /y src\ui\*.css bin\ui\ > nul
xcopy /s /y src\ui\assets\* bin\ui\assets\ > nul

echo Build complete! Starting application...
java -cp "bin;lib\postgresql-42.7.3.jar" --module-path "lib\javafx-sdk-17.0.19\lib" --add-modules javafx.controls,javafx.fxml Main

del sources.txt
