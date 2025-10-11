# Docker Quick Start Script for Windows PowerShell
# Athena Backend Application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Athena Backend - Docker Deployment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker info | Out-Null
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}

# Check if .env file exists
if (-Not (Test-Path ".env")) {
    Write-Host "⚠ .env file not found. Creating from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "✓ Created .env file. Please update it with your credentials." -ForegroundColor Green
    Write-Host ""
    Write-Host "Press any key to continue after updating .env file..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Ask user for deployment option
Write-Host ""
Write-Host "Select deployment option:" -ForegroundColor Cyan
Write-Host "1. Quick start (use pre-built JAR)" -ForegroundColor White
Write-Host "2. Full build (build inside Docker)" -ForegroundColor White
Write-Host "3. Development mode (rebuild backend only)" -ForegroundColor White
Write-Host ""
$choice = Read-Host "Enter choice (1-3)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Building application with Maven..." -ForegroundColor Yellow
        mvn clean package -DskipTests
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Maven build failed!" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "✓ Build successful" -ForegroundColor Green
        Write-Host ""
        Write-Host "Starting Docker containers..." -ForegroundColor Yellow
        docker-compose up -d
    }
    "2" {
        Write-Host ""
        Write-Host "Building and starting Docker containers..." -ForegroundColor Yellow
        docker-compose up -d --build
    }
    "3" {
        Write-Host ""
        Write-Host "Rebuilding backend service..." -ForegroundColor Yellow
        docker-compose up -d --build backend
    }
    default {
        Write-Host "Invalid choice. Exiting." -ForegroundColor Red
        exit 1
    }
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Docker deployment failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Deployment Successful!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Wait for services to be healthy
Write-Host "Waiting for services to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Check service status
Write-Host ""
Write-Host "Service Status:" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Access Your Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "API Base URL:    http://localhost:8080" -ForegroundColor White
Write-Host "Swagger UI:      http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "Health Check:    http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Useful Commands" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "View logs:       docker-compose logs -f" -ForegroundColor White
Write-Host "Stop services:   docker-compose down" -ForegroundColor White
Write-Host "Restart:         docker-compose restart" -ForegroundColor White
Write-Host ""
Write-Host "Happy coding! 🚀" -ForegroundColor Green
