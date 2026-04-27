$ErrorActionPreference = "Stop"

Write-Host "Cleaning bin directory..."
if (Test-Path "bin") {
    Remove-Item -Recurse -Force "bin\*"
} else {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Finding Java sources..."
Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName > sources.txt

Write-Host "Compiling Java sources..."
javac -d bin -cp "lib\postgresql-42.7.3.jar" --module-path "lib\javafx-sdk-17.0.19\lib" --add-modules javafx.controls,javafx.fxml @sources.txt

Write-Host "Copying resources (FXML, CSS, Assets)..."
if (!(Test-Path "bin\ui")) { New-Item -ItemType Directory -Path "bin\ui" | Out-Null }
if (!(Test-Path "bin\ui\assets")) { New-Item -ItemType Directory -Path "bin\ui\assets" | Out-Null }

Copy-Item -Path "src\ui\*.fxml" -Destination "bin\ui\" -Force
Copy-Item -Path "src\ui\*.css" -Destination "bin\ui\" -Force
Copy-Item -Path "src\ui\assets\*" -Destination "bin\ui\assets\" -Force

Write-Host "Build complete! Starting application..."
java -cp "bin;lib\postgresql-42.7.3.jar" --module-path "lib\javafx-sdk-17.0.19\lib" --add-modules javafx.controls,javafx.fxml Main

Remove-Item "sources.txt"
