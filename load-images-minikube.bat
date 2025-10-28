@echo off
echo ============================================
echo CARGANDO IMAGENES EN MINIKUBE
echo ============================================
echo.

echo Verificando que Minikube esté funcionando...
minikube status
if %errorlevel% neq 0 (
    echo Error: Minikube no está funcionando correctamente
    exit /b 1
)

echo.
echo ============================================
echo CARGANDO IMAGENES DE MICROSERVICIOS
echo ============================================
echo.

echo 1. Cargando API Gateway...
minikube image load selimhorri/api-gateway-ecommerce-boot:0.1.0
if %errorlevel% neq 0 (
    echo Error cargando API Gateway
    exit /b 1
)
echo ✓ API Gateway cargado

echo.
echo 2. Cargando Service Discovery...
minikube image load selimhorri/service-discovery-ecommerce-boot:0.1.0
if %errorlevel% neq 0 (
    echo Error cargando Service Discovery
    exit /b 1
)
echo ✓ Service Discovery cargado

echo.
echo 3. Cargando Cloud Config...
minikube image load selimhorri/cloud-config-ecommerce-boot:0.1.0
if %errorlevel% neq 0 (
    echo Error cargando Cloud Config
    exit /b 1
)
echo ✓ Cloud Config cargado

echo.
echo 4. Cargando User Service...
minikube image load selimhorri/user-service-ecommerce-boot:0.1.0
if %errorlevel% neq 0 (
    echo Error cargando User Service
    exit /b 1
)
echo ✓ User Service cargado

echo.
echo 5. Cargando Proxy Client...
minikube image load selimhorri/proxy-client-ecommerce-boot:0.1.0
if %errorlevel% neq 0 (
    echo Error cargando Proxy Client
    exit /b 1
)
echo ✓ Proxy Client cargado

echo.
echo 6. Cargando Zipkin...
minikube image load openzipkin/zipkin:latest
if %errorlevel% neq 0 (
    echo Error cargando Zipkin
    exit /b 1
)
echo ✓ Zipkin cargado

echo.
echo ============================================
echo VERIFICANDO IMAGENES CARGADAS
echo ============================================
echo.

echo Listando todas las imágenes en Minikube:
minikube image ls | findstr -E "(selimhorri|zipkin)"

echo.
echo ============================================
echo ¡TODAS LAS IMAGENES CARGADAS EXITOSAMENTE!
echo ============================================
echo.
echo Imágenes disponibles para despliegue:
echo - API Gateway (selimhorri/api-gateway-ecommerce-boot:0.1.0)
echo - Service Discovery (selimhorri/service-discovery-ecommerce-boot:0.1.0)
echo - Cloud Config (selimhorri/cloud-config-ecommerce-boot:0.1.0)
echo - User Service (selimhorri/user-service-ecommerce-boot:0.1.0)
echo - Proxy Client (selimhorri/proxy-client-ecommerce-boot:0.1.0)
echo - Zipkin (openzipkin/zipkin:latest)
echo.
echo Ahora puedes desplegar con: kubectl apply -f k8s-all-in-one.yaml