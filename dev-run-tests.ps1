# dev-run-tests.ps1
# Requisitos:
# - Docker Desktop corriendo
# - postman CLI instalado y logueado (postman login)
# - Actuator disponible en /actuator/health

$ErrorActionPreference = "Stop"

# ---- Config (ajustable) ----
$healthUrl = "http://localhost:8080/actuator/health"

$collectionId = "38883854-3d510d9f-20e6-4b36-9ecb-dad5324b831a"
$environmentId = "38883854-739a5e69-3fc2-45c4-a6b7-05dd2381f298"

# Si tu compose no se llama "docker-compose.yml" / "compose.yml", ajustá acá:
$composeCmd = "docker compose"

# ---- Helpers ----
function Require-Command($cmd) {
  if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
    throw "No encuentro '$cmd' en PATH. Instalalo o reiniciá la terminal."
  }
}

function Wait-For-Http($url, $timeoutSeconds = 120) {
  Write-Host "Esperando API listo en $url ..."
  $start = Get-Date
  while ((Get-Date) - $start -lt [TimeSpan]::FromSeconds($timeoutSeconds)) {
    try {
      $resp = Invoke-WebRequest -UseBasicParsing -Uri $url -TimeoutSec 3
      if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
        Write-Host "OK: API responde (HTTP $($resp.StatusCode))."
        return
      }
    } catch {
      # sigue intentando
    }
    Start-Sleep -Seconds 2
  }
  throw "Timeout: el API no respondió en $timeoutSeconds segundos."
}

# ---- Checks ----
Require-Command docker
Require-Command postman

Write-Host "==> Levantando stack con Docker Compose..."
Invoke-Expression "$composeCmd up -d --build"

try {
  # Espera API
  Wait-For-Http $healthUrl 120

  # Ejecuta colección
  Write-Host "==> Corriendo colección Postman CLI..."
  postman collection run $collectionId -e $environmentId

  if ($LASTEXITCODE -ne 0) {
    throw "Postman CLI devolvió código $LASTEXITCODE (tests fallaron)."
  }

  Write-Host "==> Listo: colección OK."
}
catch {
  Write-Host "`n==> ERROR: $($_.Exception.Message)" -ForegroundColor Red
  Write-Host "`n==> Logs del servicio api:" -ForegroundColor Yellow
  Invoke-Expression "$composeCmd logs api"
  exit 1
}
finally {
  # Si querés que SIEMPRE baje los contenedores al terminar, descomentá esto:
  Write-Host "`n==> Bajando stack..."
  Invoke-Expression "$composeCmd down -v"
}
