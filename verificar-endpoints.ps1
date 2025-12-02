# Script para verificar todos los endpoints de la API
# Ejecutar: .\verificar-endpoints.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔍 VERIFICADOR DE ENDPOINTS DE API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Obtener IP local
$ip = (Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "Wi-Fi*" | Select-Object -First 1).IPAddress
if (-not $ip) {
    $ip = (Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "Ethernet*" | Select-Object -First 1).IPAddress
}
if (-not $ip) {
    $ip = "localhost"
}

$baseUrl = "http://${ip}:5120"
Write-Host "📍 URL Base: $baseUrl" -ForegroundColor Yellow
Write-Host ""

# Verificar que la API esté corriendo
Write-Host "1️⃣  Verificando que la API esté corriendo..." -ForegroundColor White
$tcpConnection = Get-NetTCPConnection -LocalPort 5120 -ErrorAction SilentlyContinue
if ($tcpConnection) {
    Write-Host "   ✅ API corriendo en puerto 5120" -ForegroundColor Green
} else {
    Write-Host "   ❌ API NO está corriendo en puerto 5120" -ForegroundColor Red
    Write-Host "   💡 Inicia la API con: dotnet run" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Lista de endpoints a verificar
$endpoints = @(
    @{Name="Alertas"; Url="/api/Alerta"; Description="Lista de alertas"},
    @{Name="Reportes"; Url="/api/Reporte"; Description="Lista de reportes"},
    @{Name="Tipos SLA"; Url="/api/reporte/tipos-sla-disponibles"; Description="Tipos de SLA configurados"},
    @{Name="Áreas"; Url="/api/reporte/areas-disponibles"; Description="Áreas disponibles"},
    @{Name="Años"; Url="/api/reporte/anios-disponibles"; Description="Años con datos"},
    @{Name="Meses 2025"; Url="/api/reporte/meses-disponibles?anio=2025"; Description="Meses con datos en 2025"},
    @{Name="Tendencia SLA001"; Url="/api/reporte/solicitudes-tendencia?tipoSla=SLA001&anio=2025"; Description="Datos de tendencia SLA001"}
)

$resultados = @()
$exitosos = 0
$fallidos = 0

Write-Host "2️⃣  Probando endpoints..." -ForegroundColor White
Write-Host ""

foreach ($endpoint in $endpoints) {
    $fullUrl = "$baseUrl$($endpoint.Url)"
    Write-Host "   🔹 $($endpoint.Name)" -ForegroundColor Cyan
    Write-Host "      URL: $fullUrl" -ForegroundColor Gray

    try {
        $response = Invoke-WebRequest -Uri $fullUrl -Method GET -TimeoutSec 5 -ErrorAction Stop

        if ($response.StatusCode -eq 200) {
            $content = $response.Content | ConvertFrom-Json

            if ($content -is [Array]) {
                $count = $content.Count
            } elseif ($content.PSObject.Properties) {
                $count = $content.PSObject.Properties.Count
            } else {
                $count = 1
            }

            Write-Host "      ✅ OK (200) - $count elemento(s)" -ForegroundColor Green
            $exitosos++

            $resultados += @{
                Name = $endpoint.Name
                Status = "✅ OK"
                Code = 200
                Count = $count
                Url = $fullUrl
            }
        } else {
            Write-Host "      ⚠️  Código: $($response.StatusCode)" -ForegroundColor Yellow
            $resultados += @{
                Name = $endpoint.Name
                Status = "⚠️  Advertencia"
                Code = $response.StatusCode
                Count = 0
                Url = $fullUrl
            }
        }
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "N/A" }
        $errorMsg = $_.Exception.Message

        Write-Host "      ❌ ERROR ($statusCode)" -ForegroundColor Red
        Write-Host "      Mensaje: $errorMsg" -ForegroundColor Red
        $fallidos++

        $resultados += @{
            Name = $endpoint.Name
            Status = "❌ Error"
            Code = $statusCode
            Count = 0
            Url = $fullUrl
            Error = $errorMsg
        }
    }
    Write-Host ""
}

# Resumen
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 RESUMEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Exitosos: $exitosos / $($endpoints.Count)" -ForegroundColor Green
Write-Host "Fallidos:  $fallidos / $($endpoints.Count)" -ForegroundColor Red
Write-Host ""

# Tabla de resultados
Write-Host "Detalle:" -ForegroundColor White
foreach ($resultado in $resultados) {
    $status = $resultado.Status
    $name = $resultado.Name.PadRight(20)
    $count = if ($resultado.Count -gt 0) { "($($resultado.Count) items)" } else { "" }

    Write-Host "  $status $name $count"
}
Write-Host ""

# Verificar base de datos
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🗄️  VERIFICACIÓN DE DATOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$endpointsConDatos = $resultados | Where-Object { $_.Count -gt 0 }
$endpointsSinDatos = $resultados | Where-Object { $_.Status -eq "✅ OK" -and $_.Count -eq 0 }

if ($endpointsSinDatos.Count -gt 0) {
    Write-Host "⚠️  Los siguientes endpoints responden pero NO tienen datos:" -ForegroundColor Yellow
    foreach ($endpoint in $endpointsSinDatos) {
        Write-Host "   • $($endpoint.Name)" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "💡 Esto significa que las tablas en la base de datos están vacías." -ForegroundColor Yellow
    Write-Host "   Necesitas insertar datos de prueba en:" -ForegroundColor Yellow
    Write-Host "   - Tabla: alerta (para Alertas)" -ForegroundColor Gray
    Write-Host "   - Tabla: reporte (para Reportes)" -ForegroundColor Gray
    Write-Host "   - Tabla: config_sla (para Tipos SLA)" -ForegroundColor Gray
    Write-Host "   - Tabla: solicitud (para datos históricos)" -ForegroundColor Gray
    Write-Host ""
}

# Recomendaciones finales
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "💡 RECOMENDACIONES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($fallidos -eq 0 -and $exitosos -eq $endpoints.Count) {
    Write-Host "✅ ¡Todos los endpoints funcionan correctamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Siguiente paso:" -ForegroundColor White
    Write-Host "1. Recompila la app: Build → Rebuild Project" -ForegroundColor Gray
    Write-Host "2. Instala en dispositivo: Run" -ForegroundColor Gray
    Write-Host "3. Verifica logs en Logcat (filtro: Proyecto1App)" -ForegroundColor Gray
} elseif ($fallidos -gt 0) {
    Write-Host "❌ Algunos endpoints tienen problemas." -ForegroundColor Red
    Write-Host ""
    Write-Host "Acciones requeridas:" -ForegroundColor White
    foreach ($resultado in $resultados | Where-Object { $_.Status -ne "✅ OK" }) {
        Write-Host "• $($resultado.Name):" -ForegroundColor Yellow
        Write-Host "  URL: $($resultado.Url)" -ForegroundColor Gray
        if ($resultado.Error) {
            Write-Host "  Error: $($resultado.Error)" -ForegroundColor Red
        }
        Write-Host "  💡 Verifica el controlador en el backend" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Para más información, consulta:" -ForegroundColor White
Write-Host "📄 SOLUCION_MODULOS_SIN_DATOS.md" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

