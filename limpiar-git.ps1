# =============================
# Script de limpieza de Git
# Elimina archivos ya commitados que ahora están en .gitignore
# =============================

Write-Host "=== Limpieza de archivos innecesarios del repositorio ===" -ForegroundColor Cyan
Write-Host ""

# Ir al directorio del proyecto
Set-Location "D:\REPOS\Sistema de control"

Write-Host "Paso 1: Verificando archivos rastreados que deberían ignorarse..." -ForegroundColor Yellow
Write-Host ""

# Listar archivos problemáticos ya commitados
$archivosProblematicos = @(
    "build/",
    ".gradle/",
    ".idea/",
    "*.iml",
    "*.class",
    "*.log",
    "*.apk",
    "*.dex",
    "local.properties",
    "app/build/",
    "gradle/",
    ".DS_Store"
)

Write-Host "Buscando archivos que coincidan con .gitignore..." -ForegroundColor White
$encontrados = git ls-files | Where-Object {
    $archivo = $_
    $archivosProblematicos | Where-Object { $archivo -like "*$_*" }
}

if ($encontrados) {
    Write-Host "Archivos encontrados que deberían ser ignorados:" -ForegroundColor Red
    $encontrados | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
    Write-Host ""
} else {
    Write-Host "✓ No se encontraron archivos problemáticos ya commitados." -ForegroundColor Green
    Write-Host ""
    exit 0
}

Write-Host "Paso 2: ¿Deseas eliminar estos archivos del repositorio?" -ForegroundColor Yellow
Write-Host "Nota: Los archivos NO se borrarán de tu disco, solo dejarán de ser rastreados por Git." -ForegroundColor Cyan
Write-Host ""
$respuesta = Read-Host "Escribe 'SI' para continuar o cualquier otra cosa para cancelar"

if ($respuesta -ne "SI") {
    Write-Host "Operación cancelada." -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "Paso 3: Eliminando archivos del índice de Git..." -ForegroundColor Yellow

# Eliminar todo del índice
git rm -r --cached .

Write-Host "✓ Archivos eliminados del índice" -ForegroundColor Green
Write-Host ""

Write-Host "Paso 4: Volviendo a agregar archivos (respetando .gitignore)..." -ForegroundColor Yellow

# Re-agregar todo respetando .gitignore
git add .

Write-Host "✓ Archivos agregados nuevamente" -ForegroundColor Green
Write-Host ""

Write-Host "Paso 5: Verificando cambios..." -ForegroundColor Yellow
Write-Host ""

# Mostrar status
git status --short

Write-Host ""
Write-Host "=== Resumen ===" -ForegroundColor Cyan
Write-Host "Los archivos marcados con 'D' (deleted) han sido eliminados del repositorio." -ForegroundColor White
Write-Host "Los archivos marcados con 'M' (modified) son tus archivos de código actualizados." -ForegroundColor White
Write-Host ""

Write-Host "Paso 6: ¿Deseas hacer commit de estos cambios?" -ForegroundColor Yellow
$respuesta2 = Read-Host "Escribe 'SI' para commitear o cualquier otra cosa para revisar manualmente"

if ($respuesta2 -eq "SI") {
    git commit -m "chore: aplicar .gitignore y remover archivos de build/cache del repositorio"
    Write-Host ""
    Write-Host "✓ Commit realizado exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximo paso: Ejecuta 'git push origin main' (o tu rama) para subir los cambios" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Cambios preparados pero NO commitados." -ForegroundColor Yellow
    Write-Host "Revisa con: git status" -ForegroundColor Cyan
    Write-Host "Para commitear: git commit -m 'chore: aplicar .gitignore y limpiar repo'" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "=== Proceso completado ===" -ForegroundColor Green

