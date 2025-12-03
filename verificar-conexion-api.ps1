# Script para verificar conexión con la API
# IP del servidor: 172.19.9.109:5120

$serverIp = "172.19.9.109"
$serverPort = 5120
$testEndpoint = "http://${serverIp}:${serverPort}/api/reporte/tipos-sla-disponibles"

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Verificación de Conexión API" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔍 Servidor configurado: $serverIp" -ForegroundColor Yellow
Write-Host "🔍 Puerto: $serverPort" -ForegroundColor Yellow
Write-Host "🔍 URL completa: http://${serverIp}:${serverPort}/" -ForegroundColor Yellow
Write-Host ""

# 1. Test de ping
Write-Host "1️⃣ Probando conectividad (ping)..." -ForegroundColor Magenta
$pingResult = Test-Connection -ComputerName $serverIp -Count 2 -Quiet
if ($pingResult) {
    Write-Host "   ✅ El servidor responde a ping" -ForegroundColor Green
} else {
    Write-Host "   ❌ El servidor NO responde a ping" -ForegroundColor Red
    Write-Host "   ⚠️ Verifica que:" -ForegroundColor Yellow
    Write-Host "      - El servidor esté encendido" -ForegroundColor White
    Write-Host "      - Estés en la misma red (WiFi o cable)" -ForegroundColor White
    Write-Host "      - El firewall permita ping" -ForegroundColor White
}
Write-Host ""

# 2. Test de puerto
Write-Host "2️⃣ Probando puerto $serverPort..." -ForegroundColor Magenta
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $tcpClient.Connect($serverIp, $serverPort)
    Write-Host "   ✅ Puerto $serverPort está abierto" -ForegroundColor Green
    $tcpClient.Close()
} catch {
    Write-Host "   ❌ Puerto $serverPort NO está accesible" -ForegroundColor Red
    Write-Host "   ⚠️ Verifica que:" -ForegroundColor Yellow
    Write-Host "      - La API esté ejecutándose" -ForegroundColor White
    Write-Host "      - El firewall del servidor permita el puerto $serverPort" -ForegroundColor White
}
Write-Host ""

# 3. Test HTTP
Write-Host "3️⃣ Probando endpoint de la API..." -ForegroundColor Magenta
try {
    $response = Invoke-WebRequest -Uri $testEndpoint -TimeoutSec 5 -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        Write-Host "   ✅ API respondiendo correctamente" -ForegroundColor Green
        Write-Host "   📊 Status Code: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "   📄 Respuesta: $($response.Content.Substring(0, [Math]::Min(100, $response.Content.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "   ❌ Error al conectar con la API" -ForegroundColor Red
    Write-Host "   📄 Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "   ⚠️ Posibles soluciones:" -ForegroundColor Yellow
    Write-Host "      - Verifica que la API esté ejecutándose en el servidor" -ForegroundColor White
    Write-Host "      - Ejecuta en el servidor: dotnet run (o el comando apropiado)" -ForegroundColor White
    Write-Host "      - Verifica el firewall en ambas máquinas" -ForegroundColor White
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Información de Red Local" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Mostrar IP local
$localIp = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.InterfaceAlias -notlike "*Loopback*" -and $_.IPAddress -notlike "169.254.*" } | Select-Object -First 1).IPAddress
Write-Host "📱 Tu IP local: $localIp" -ForegroundColor Cyan
Write-Host ""

# Verificar si están en la misma subred
if ($localIp -and $localIp.StartsWith("172.19.9.")) {
    Write-Host "✅ Estás en la misma subred que el servidor (172.19.9.x)" -ForegroundColor Green
} else {
    Write-Host "⚠️ NO estás en la misma subred que el servidor" -ForegroundColor Yellow
    Write-Host "   Servidor: 172.19.9.x" -ForegroundColor White
    Write-Host "   Tu IP: $localIp" -ForegroundColor White
    Write-Host ""
    Write-Host "   💡 Solución: Conéctate a la misma red WiFi que el servidor" -ForegroundColor Cyan
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Siguiente Paso" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

if ($pingResult -and $response) {
    Write-Host "✅ Todo OK - Puedes compilar e instalar la app" -ForegroundColor Green
    Write-Host ""
    Write-Host "Ejecuta:" -ForegroundColor Cyan
    Write-Host "  .\probar-correcciones.ps1" -ForegroundColor White
} else {
    Write-Host "❌ Hay problemas de conexión - Revisa los errores arriba" -ForegroundColor Red
    Write-Host ""
    Write-Host "Checklist:" -ForegroundColor Yellow
    Write-Host "  [ ] Servidor encendido" -ForegroundColor White
    Write-Host "  [ ] API ejecutándose en puerto $serverPort" -ForegroundColor White
    Write-Host "  [ ] Ambos dispositivos en la misma red" -ForegroundColor White
    Write-Host "  [ ] Firewall configurado" -ForegroundColor White
}
Write-Host ""

