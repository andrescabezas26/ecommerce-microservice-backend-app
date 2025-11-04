    # Script para construir todas las imagenes Docker en Minikube
    Write-Host "Construyendo imagenes Docker para Minikube..." -ForegroundColor Cyan

    # Configurar Docker para usar el daemon de Minikube
    Write-Host "Configurando Docker para Minikube..." -ForegroundColor Yellow
    & minikube docker-env --shell powershell | Invoke-Expression
    Write-Host "[OK] Docker apunta a Minikube" -ForegroundColor Green

    # Lista de servicios
    $services = @(
        "service-discovery",
        "cloud-config",
        "api-gateway",
        "proxy-client",
        "user-service",
        "product-service",
        "order-service",
        "payment-service",
        "shipping-service",
        "favourite-service"
    )

    $version = "0.1.0"
    $successCount = 0
    $failCount = 0

    foreach ($service in $services) {
        Write-Host ""
        Write-Host "================================================" -ForegroundColor Cyan
        Write-Host "[BUILDING] Compilando $service con Maven..." -ForegroundColor Green
        Write-Host "================================================" -ForegroundColor Cyan
        
        Set-Location $service
        
        # Compilar con Maven
        Write-Host "[MAVEN] Compilando $service..." -ForegroundColor Yellow
        & mvn clean package -DskipTests -q
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERROR] Maven falló en $service" -ForegroundColor Red
            Set-Location ..
            $failCount++
            continue
        }
        
        Write-Host "[OK] Maven compilación exitosa" -ForegroundColor Green
        
        # Construir imagen Docker
        Write-Host "[DOCKER] Construyendo imagen en Minikube..." -ForegroundColor Yellow
        docker build -t "${service}:${version}" -t "${service}:latest" .
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] $service construido exitosamente" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "[ERROR] Error construyendo $service" -ForegroundColor Red
            $failCount++
        }
        
        Set-Location ..
    }

    Write-Host ""
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "Resumen de Construccion" -ForegroundColor Cyan
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "[OK] Exitosas: $successCount" -ForegroundColor Green
    Write-Host "[ERROR] Fallidas: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })

    Write-Host ""
    Write-Host "Listando imagenes construidas:" -ForegroundColor Cyan
    docker images | Select-String -Pattern "service|gateway|proxy|cloud"

    Write-Host ""
    if ($failCount -eq 0) {
        Write-Host "Todas las imagenes construidas exitosamente!" -ForegroundColor Green
    } else {
        Write-Host "$failCount imagenes fallaron en la construccion" -ForegroundColor Red
    }
