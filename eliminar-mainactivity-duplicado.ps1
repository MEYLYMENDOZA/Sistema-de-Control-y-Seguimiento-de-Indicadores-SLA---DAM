# Script para eliminar el MainActivity duplicado en la carpeta presentation
$archivoDuplicado = "c:\Users\meyle\AndroidStudioProjects\Proyecto1\app\src\main\java\com\example\proyecto1\presentation\MainActivity.kt"

if (Test-Path $archivoDuplicado) {
    Remove-Item $archivoDuplicado -Force
    Write-Host "✅ MainActivity duplicado eliminado exitosamente de la carpeta presentation" -ForegroundColor Green
} else {
    Write-Host "⚠️ El archivo ya no existe o fue eliminado previamente" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

