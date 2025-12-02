Write-Host "🔄 Reiniciando backend con cambios de RolRegistro..." -ForegroundColor Cyan

# Detener procesos dotnet existentes (excepto Visual Studio)
Write-Host "⏹️  Deteniendo procesos dotnet anteriores..." -ForegroundColor Yellow
$dotnetProcesses = Get-Process -Name dotnet -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -notlike "*Visual Studio*" }
if ($dotnetProcesses) {
    $dotnetProcesses | Stop-Process -Force
    Write-Host "✅ Procesos anteriores detenidos" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No hay procesos dotnet ejecutándose" -ForegroundColor Gray
}

# Esperar un momento
Start-Sleep -Seconds 2

# Buscar el proyecto .csproj
Write-Host "`n🔍 Buscando proyecto backend..." -ForegroundColor Cyan
$projectPath = Get-ChildItem -Path "D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM" -Filter "*.csproj" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1

if ($projectPath) {
    $projectDir = Split-Path $projectPath.FullName -Parent
    Write-Host "✅ Proyecto encontrado: $($projectPath.Name)" -ForegroundColor Green
    Write-Host "📁 Directorio: $projectDir" -ForegroundColor Gray

    # Cambiar al directorio del proyecto
    Set-Location $projectDir

    Write-Host "`n🚀 Iniciando backend..." -ForegroundColor Cyan
    Write-Host "⏱️  Esto tomará unos segundos..." -ForegroundColor Gray
    Write-Host "`nPresiona Ctrl+C para detener el servidor cuando termines de probar.`n" -ForegroundColor Yellow

    # Ejecutar dotnet run
    dotnet run

} else {
    Write-Host "❌ No se encontró ningún proyecto .csproj" -ForegroundColor Red
    Write-Host "ℹ️  Verifica que el proyecto esté en:" -ForegroundColor Yellow
    Write-Host "   D:\REPOS\Sistema-de-Control-y-Seguimiento-de-Indicadores-SLA---DAM" -ForegroundColor Gray
}
