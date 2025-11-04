# E2E Tests Runner Script for Minikube Environment
# This script runs E2E tests against services already deployed in Minikube

param(
    [switch]$Verbose,
    [switch]$SkipCleanup,
    [string]$Environment = "minikube",
    [string]$Namespace = "ecommerce-microservices"
)

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Cyan"

# Configuration
$E2EDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ResultsDir = Join-Path $E2EDir "results"

Write-Host "🚀 Starting E-Commerce E2E Test Suite (Minikube)" -ForegroundColor $Blue
Write-Host "===============================================" -ForegroundColor $Blue
Write-Host ""

# Create results directory
if (-not (Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir -Force | Out-Null
}

try {
    # Check if Minikube is running
    Write-Host "🔍 Checking Minikube status..." -ForegroundColor $Blue
    $minikubeStatus = minikube status 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "💥 Minikube is not running. Please start Minikube first:" -ForegroundColor $Red
        Write-Host "  minikube start" -ForegroundColor $Yellow
        exit 1
    }
    
    Write-Host "✅ Minikube is running" -ForegroundColor $Green
    
    # Check if services are deployed
    Write-Host "🔍 Checking if services are deployed in namespace '$Namespace'..." -ForegroundColor $Blue
    $namespaceCheck = kubectl get namespace $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "💥 Namespace '$Namespace' does not exist" -ForegroundColor $Red
        Write-Host "Please deploy the services first or check the namespace name" -ForegroundColor $Yellow
        exit 1
    }
    
    # List running pods
    Write-Host "📋 Current pods in namespace '$Namespace':" -ForegroundColor $Blue
    $pods = kubectl get pods -n $Namespace --no-headers 2>$null
    if ($pods) {
        $pods.Split("`n") | ForEach-Object {
            if ($_.Trim()) {
                $parts = $_.Split()
                $podName = $parts[0]
                $podStatus = $parts[2]
                if ($podStatus -eq "Running") {
                    Write-Host "  ✅ ${podName}: $podStatus" -ForegroundColor $Green
                } else {
                    Write-Host "  ⚠️  ${podName}: $podStatus" -ForegroundColor $Yellow
                }
            }
        }
    }
    
    # Check if API Gateway service is exposed
    Write-Host ""
    Write-Host "🔍 Checking API Gateway service exposure..." -ForegroundColor $Blue
    $serviceCheck = kubectl get service api-gateway-service -n $Namespace 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "💥 API Gateway service not found" -ForegroundColor $Red
        exit 1
    }
    
    # Get the URL for API Gateway
    $apiGatewayUrl = (minikube service api-gateway-service -n $Namespace --url 2>$null) | Select-Object -First 1
    if (-not $apiGatewayUrl) {
        Write-Host "💥 Could not get API Gateway URL" -ForegroundColor $Red
        exit 1
    }
    
    Write-Host "✅ API Gateway accessible at: $apiGatewayUrl" -ForegroundColor $Green
    
    # Update Minikube environment with actual URL
    Set-Location $E2EDir
    Write-Host "📝 Updating Minikube environment configuration..." -ForegroundColor $Blue
    
    # Create temporary environment file with actual URLs
    $tempEnvContent = @{
        id = "minikube-environment-temp"
        name = "Minikube Environment (Temp)"
        values = @(
            @{
                key = "base_url"
                value = $apiGatewayUrl
                enabled = $true
                type = "default"
            },
            @{
                key = "service_discovery_url"
                value = $apiGatewayUrl
                enabled = $true
                type = "default"
            },
            @{
                key = "proxy_client_url"
                value = $apiGatewayUrl
                enabled = $true
                type = "default"
            },
            @{
                key = "config_server_url"
                value = $apiGatewayUrl
                enabled = $true
                type = "default"
            }
        )
        _postman_variable_scope = "environment"
    }
    
    $tempEnvContent | ConvertTo-Json -Depth 5 | Out-File -FilePath "minikube-environment-temp.json" -Encoding UTF8
    
    # Install dependencies if not present
    if (-not (Test-Path "node_modules")) {
        Write-Host "📦 Installing Newman dependencies..." -ForegroundColor $Yellow
        npm install --legacy-peer-deps
    }
    
    # Run E2E tests directly against Minikube
    Write-Host ""
    Write-Host "🧪 Running E2E tests against Minikube services..." -ForegroundColor $Blue
    
    if ($Verbose) {
        newman run ecommerce-e2e-collection.json -e minikube-environment-temp.json --reporters cli,json,htmlextra --reporter-json-export results/newman-report.json --reporter-htmlextra-export results/newman-report.html --delay-request 1000 --timeout-request 15000 --verbose
    } else {
        newman run ecommerce-e2e-collection.json -e minikube-environment-temp.json --reporters cli,json,htmlextra --reporter-json-export results/newman-report.json --reporter-htmlextra-export results/newman-report.html --delay-request 1000 --timeout-request 15000
    }
    
    # Cleanup temporary environment file
    if (Test-Path "minikube-environment-temp.json") {
        Remove-Item "minikube-environment-temp.json" -Force
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "🎉 E2E tests completed successfully!" -ForegroundColor $Green
        Write-Host "📊 Test results available in: $ResultsDir" -ForegroundColor $Green
        
        # Display summary if JSON report exists
        $reportPath = Join-Path $ResultsDir "newman-report.json"
        if (Test-Path $reportPath) {
            Write-Host ""
            Write-Host "📋 Test Summary:" -ForegroundColor $Blue
            
            $report = Get-Content $reportPath | ConvertFrom-Json
            $stats = $report.run.stats
            
            Write-Host "  Total Requests: $($stats.requests.total)"
            Write-Host "  ✅ Passed: $($stats.requests.total - $stats.requests.failed)" -ForegroundColor $Green
            Write-Host "  ❌ Failed: $($stats.requests.failed)" -ForegroundColor $Red
            Write-Host "  ⏱️  Average Response Time: $([Math]::Round($stats.requests.average))ms"
        }
        
        $htmlReportPath = Join-Path $ResultsDir "newman-report.html"
        if (Test-Path $htmlReportPath) {
            Write-Host ""
            Write-Host "🌐 View detailed report: file:///$htmlReportPath" -ForegroundColor $Green
        }
    } else {
        Write-Host ""
        Write-Host "💥 E2E tests failed!" -ForegroundColor $Red
        Write-Host "📊 Check test results in: $ResultsDir" -ForegroundColor $Red
        exit 1
    }
}
catch {
    Write-Host "💥 Unexpected error: $_" -ForegroundColor $Red
    exit 1
}