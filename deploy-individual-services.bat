@echo off
echo Desplegando microservicios desde archivos individuales...

REM Primero aplicamos el namespace
echo.
echo [1/12] Aplicando namespace...
kubectl apply -f k8s\00-namespace.yaml
if errorlevel 1 (
    echo Error al crear el namespace
    exit /b 1
)

REM Zipkin (sin dependencias)
echo.
echo [2/12] Desplegando Zipkin...
kubectl apply -f k8s\01-zipkin.yaml
if errorlevel 1 (
    echo Error al desplegar Zipkin
    exit /b 1
)

REM Service Discovery (depende solo de Zipkin)
echo.
echo [3/12] Desplegando Service Discovery...
kubectl apply -f k8s\02-service-discovery.yaml
if errorlevel 1 (
    echo Error al desplegar Service Discovery
    exit /b 1
)

REM Esperamos que Service Discovery esté listo
echo Esperando que Service Discovery esté listo...
kubectl wait --for=condition=ready pod -l app=service-discovery -n ecommerce-microservices --timeout=300s

REM Cloud Config (depende de Service Discovery)
echo.
echo [4/12] Desplegando Cloud Config...
kubectl apply -f k8s\03-cloud-config.yaml
if errorlevel 1 (
    echo Error al desplegar Cloud Config
    exit /b 1
)

REM Esperamos que Cloud Config esté listo
echo Esperando que Cloud Config esté listo...
kubectl wait --for=condition=ready pod -l app=cloud-config -n ecommerce-microservices --timeout=300s

REM User Service
echo.
echo [5/12] Desplegando User Service...
kubectl apply -f k8s\04-user-service.yaml
if errorlevel 1 (
    echo Error al desplegar User Service
    exit /b 1
)

REM API Gateway
echo.
echo [6/12] Desplegando API Gateway...
kubectl apply -f k8s\05-api-gateway.yaml
if errorlevel 1 (
    echo Error al desplegar API Gateway
    exit /b 1
)

REM Proxy Client
echo.
echo [7/12] Desplegando Proxy Client...
kubectl apply -f k8s\06-proxy-client.yaml
if errorlevel 1 (
    echo Error al desplegar Proxy Client
    exit /b 1
)

REM Order Service
echo.
echo [8/12] Desplegando Order Service...
kubectl apply -f k8s\07-order-service.yaml
if errorlevel 1 (
    echo Error al desplegar Order Service
    exit /b 1
)

REM Payment Service
echo.
echo [9/12] Desplegando Payment Service...
kubectl apply -f k8s\08-payment-service.yaml
if errorlevel 1 (
    echo Error al desplegar Payment Service
    exit /b 1
)

REM Product Service
echo.
echo [10/12] Desplegando Product Service...
kubectl apply -f k8s\09-product-service.yaml
if errorlevel 1 (
    echo Error al desplegar Product Service
    exit /b 1
)

REM Shipping Service
echo.
echo [11/12] Desplegando Shipping Service...
kubectl apply -f k8s\10-shipping-service.yaml
if errorlevel 1 (
    echo Error al desplegar Shipping Service
    exit /b 1
)

REM Favourite Service
echo.
echo [12/12] Desplegando Favourite Service...
kubectl apply -f k8s\11-favourite-service.yaml
if errorlevel 1 (
    echo Error al desplegar Favourite Service
    exit /b 1
)

echo.
echo ============================================
echo Despliegue completado exitosamente!
echo ============================================
echo.
echo Verificando el estado de los pods...
kubectl get pods -n ecommerce-microservices

echo.
echo Para acceder a los servicios, usa los siguientes comandos:
echo - Eureka: kubectl port-forward svc/service-discovery-service 8761:8761 -n ecommerce-microservices
echo - Zipkin: kubectl port-forward svc/zipkin-service 9411:9411 -n ecommerce-microservices
echo - API Gateway: kubectl port-forward svc/api-gateway-service 8080:8080 -n ecommerce-microservices