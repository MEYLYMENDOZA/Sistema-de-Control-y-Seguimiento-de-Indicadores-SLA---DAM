# Script para eliminar archivos duplicados del proyecto
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Eliminando Archivos Duplicados" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$archivosDuplicados = @(
    "c:\Users\meyle\AndroidStudioProjects\Proyecto1\app\src\main\java\com\example\proyecto1\presentation\MainActivity.kt",
    "c:\Users\meyle\AndroidStudioProjects\Proyecto1\app\src\main\java\com\example\proyecto1\data\repository\SlaRepository_NEW.kt"
)

$eliminados = 0
$noEncontrados = 0

foreach ($archivo in $archivosDuplicados) {
    $nombreArchivo = Split-Path $archivo -Leaf

    if (Test-Path $archivo) {
        Remove-Item $archivo -Force
        Write-Host "✅ $nombreArchivo eliminado exitosamente" -ForegroundColor Green
        $eliminados++
    } else {
        Write-Host "⚠️  $nombreArchivo no encontrado (ya fue eliminado)" -ForegroundColor Yellow
        $noEncontrados++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Resumen:" -ForegroundColor White
Write-Host "  Archivos eliminados: $eliminados" -ForegroundColor Green
Write-Host "  Archivos no encontrados: $noEncontrados" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Proceso completado. Ahora haz Clean & Rebuild del proyecto en Android Studio." -ForegroundColor Green
Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

