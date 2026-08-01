$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not (Test-Path ".\.env")) {
    Write-Error "Missing .env. Copy .env.example to .env and fill in real values."
}

docker compose up --build -d

docker compose ps
