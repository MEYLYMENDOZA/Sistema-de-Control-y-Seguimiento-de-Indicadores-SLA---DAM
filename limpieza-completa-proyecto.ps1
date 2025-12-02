# Script completo de limpieza y corrección del proyecto
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Limpieza Completa del Proyecto" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Eliminar archivos duplicados
Write-Host "Paso 1: Eliminando archivos duplicados..." -ForegroundColor Yellow
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
        Write-Host "  ✅ $nombreArchivo eliminado" -ForegroundColor Green
        $eliminados++
    } else {
        Write-Host "  ⚠️  $nombreArchivo no encontrado (ya fue eliminado)" -ForegroundColor Yellow
        $noEncontrados++
    }
}

Write-Host ""
Write-Host "  📊 Archivos eliminados: $eliminados" -ForegroundColor Green
Write-Host "  📊 Archivos no encontrados: $noEncontrados" -ForegroundColor Yellow
Write-Host ""

# Paso 2: Limpiar carpetas de build
Write-Host "Paso 2: Limpiando carpetas de compilación..." -ForegroundColor Yellow
Write-Host ""

$carpetasBuild = @(
    "c:\Users\meyle\AndroidStudioProjects\Proyecto1\app\build",
    "c:\Users\meyle\AndroidStudioProjects\Proyecto1\build",
    "c:\Users\meyle\AndroidStudioProjects\Proyecto1\.gradle"
)

foreach ($carpeta in $carpetasBuild) {
    if (Test-Path $carpeta) {
        try {
            Write-Host "  🗑️  Limpiando: $(Split-Path $carpeta -Leaf)" -ForegroundColor Gray
            Remove-Item $carpeta -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "  ✅ Limpiado correctamente" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠️  No se pudo limpiar completamente (puede estar en uso)" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# Paso 3: Limpiar caché de Android Studio
Write-Host "Paso 3: Limpiando caché de Android Studio..." -ForegroundColor Yellow
Write-Host ""

$cachePaths = @(
    "$env:USERPROFILE\.gradle\caches",
    "$env:LOCALAPPDATA\Google\AndroidStudio*\caches"
)

foreach ($cachePath in $cachePaths) {
    if ($cachePath -like "*AndroidStudio*") {
        # Buscar carpetas que coincidan con el patrón
        $folders = Get-Item $cachePath -ErrorAction SilentlyContinue
        foreach ($folder in $folders) {
            Write-Host "  🗑️  Limpiando caché de Android Studio..." -ForegroundColor Gray
            try {
                Remove-Item "$folder\*" -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "  ✅ Caché limpiado" -ForegroundColor Green
            } catch {
                Write-Host "  ⚠️  Caché parcialmente limpiado" -ForegroundColor Yellow
            }
        }
    } else {
        if (Test-Path $cachePath) {
            Write-Host "  🗑️  Limpiando caché de Gradle..." -ForegroundColor Gray
            try {
                Remove-Item "$cachePath\*" -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "  ✅ Caché limpiado" -ForegroundColor Green
            } catch {
                Write-Host "  ⚠️  Caché parcialmente limpiado" -ForegroundColor Yellow
            }
        }
    }
}

Write-Host ""

# Resumen final
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ LIMPIEZA COMPLETADA" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pasos siguientes en Android Studio:" -ForegroundColor White
Write-Host ""
Write-Host "  1. Abre Android Studio" -ForegroundColor Yellow
Write-Host "  2. File → Invalidate Caches / Restart" -ForegroundColor Yellow
Write-Host "  3. Selecciona 'Invalidate and Restart'" -ForegroundColor Yellow
Write-Host "  4. Después del reinicio:" -ForegroundColor Yellow
Write-Host "     - Build → Clean Project" -ForegroundColor Cyan
Write-Host "     - Build → Rebuild Project" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

