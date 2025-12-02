# Script de Prueba para verificar respuesta del backend

Write-Host "Probando endpoints del backend..." -ForegroundColor Cyan
Write-Host ""

# 1. Probar GET /api/User
Write-Host "[1/3] GET http://localhost:5120/api/User" -ForegroundColor Yellow
try {
    $usuarios = Invoke-RestMethod -Uri "http://localhost:5120/api/User" -Method GET -ContentType "application/json"
    Write-Host "✅ Respuesta recibida:" -ForegroundColor Green
    $usuarios | ConvertTo-Json -Depth 5
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "---" -ForegroundColor Gray
Write-Host ""

# 2. Probar GET /api/User/roles (si existe)
Write-Host "[2/3] GET http://localhost:5120/api/User/roles" -ForegroundColor Yellow
try {
    $roles = Invoke-RestMethod -Uri "http://localhost:5120/api/User/roles" -Method GET -ContentType "application/json"
    Write-Host "✅ Respuesta recibida:" -ForegroundColor Green
    $roles | ConvertTo-Json -Depth 5
} catch {
    Write-Host "⚠️  Endpoint no existe o error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "---" -ForegroundColor Gray
Write-Host ""

# 3. Probar estructura de respuesta
Write-Host "[3/3] Analizando estructura de respuesta..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:5120/api/User" -Method GET -ContentType "application/json"

    if ($response -is [Array]) {
        Write-Host "✅ La respuesta es un Array de usuarios" -ForegroundColor Green
        Write-Host "   Total de usuarios: $($response.Count)" -ForegroundColor Gray

        if ($response.Count -gt 0) {
            Write-Host "   Estructura del primer usuario:" -ForegroundColor Gray
            $response[0] | Format-List
        }
    } elseif ($response.usuarios) {
        Write-Host "✅ La respuesta tiene formato { usuarios: [...], total: N }" -ForegroundColor Green
        Write-Host "   Total: $($response.total)" -ForegroundColor Gray
    } else {
        Write-Host "⚠️  Estructura de respuesta desconocida" -ForegroundColor Yellow
        $response | Format-List
    }
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Copia la estructura de respuesta y envíala" -ForegroundColor White
Write-Host "para ajustar los DTOs en la app Android" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan

