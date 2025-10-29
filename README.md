 # ecommerce-microservice-backend-app

 Este repositorio contiene una arquitectura de microservicios Spring Boot preparada para pruebas y despliegue en Kubernetes (Minikube). A continuación se documenta lo realizado hasta ahora y los pasos para replicar localmente el flujo: construir imágenes, cargarlas en Minikube, desplegar en Kubernetes y automatizar con GitHub Actions usando un runner local.

 ## Resumen de lo realizado

 - Se consolidaron y optimizaron los manifiestos Kubernetes en `k8s-optimized.yaml` y/o separados en `k8s/*.yaml` para cada servicio.
 - Se ajustaron recursos (CPU/memory) y sondas (readiness/liveness) para que los servicios puedan ejecutarse en Minikube con recursos limitados.
 - Se crearon scripts para cargar imágenes de Docker en Minikube (por ejemplo `load-images-minikube.bat`).
 - Se probó localmente el despliegue y se resolvieron problemas de puertos (api-gateway vs user-service) y tiempos de arranque.
 - Se añadió un workflow de GitHub Actions (`.github/workflows/dev-pipeline.yml`) pensado para ejecutarse en un runner self-hosted local que automatiza build → load images → deploy.

 ## Archivos relevantes

 - `k8s-optimized.yaml` — manifiesto Kubernetes consolidado (namespace + deployments + services)
 - `k8s/01-zipkin.yaml` ... `k8s/06-proxy-client.yaml` — manifiestos separados por servicio (si existen)
 - `compose.yml` — Docker Compose para construir las imágenes localmente
 - `load-images-minikube.bat` — script para cargar imágenes construidas en el docker daemon de Minikube
 - `.github/workflows/dev-pipeline.yml` — workflow para ejecutar build + load + deploy en un runner local

 ## Comandos importantes (rápida referencia)

 1) Levantar Minikube (Windows / PowerShell) — con ~13 GB y 4 CPUs (ajustar según memoria disponible en Docker Desktop):

 ```powershell
 minikube start --memory=12974 --cpus=4 --driver=docker
 ```

 2) Construir imágenes con Docker Compose (desde la raíz del repo):

 ```powershell
 docker compose -f ./compose.yml build
 ```

 3) Cargar imágenes en Minikube (PowerShell):

 ```powershell
 # Si tienes el script load-images-minikube.bat
 & .\load-images-minikube.bat

 # Alternativa: si usas un .sh (Linux),
 ./load-images-minikube.sh
 ```

 4) Desplegar en Kubernetes (usar el manifiesto consolidado o los separados):

 ```powershell
 # Manifiesto consolidado
 kubectl apply -f k8s-optimized.yaml

 # O desplegar todos los archivos en el directorio k8s/
 kubectl apply -f k8s/
 ```

 5) Verificar estado de pods (namespace `ecommerce-microservices`):

 ```powershell
 kubectl get pods -n ecommerce-microservices
 ```

 6) Acceder a un servicio (ej. API Gateway) via Minikube:

 ```powershell
 minikube service api-gateway-service -n ecommerce-microservices
 # o para obtener URL sin abrir browser:
 minikube service api-gateway-service -n ecommerce-microservices --url
 ```

 7) Ejecutar el GitHub Actions runner local (PowerShell):

 ```powershell
 PS C:\actions-runner> .\run.cmd
 ```

 8) Ejecutar el workflow localmente (ejecutará los pasos: build → load images → deploy):

  - Asegúrate de que el runner self-hosted esté corriendo (`./run.cmd`).
  - Haz push a la rama `master` / `main` o triggerea el workflow localmente según configuración.

 ## Detalles y notas prácticas

 - Puertos y sondas: varias aplicaciones arrancan en puertos distintos (por ejemplo `api-gateway` en 8080 y `user-service` en 8700). Verifica la configuración del `containerPort`, `readinessProbe` y `livenessProbe` para cada deployment.
 - Si un pod entra en `CrashLoopBackOff`, revisa los logs con:

 ```powershell
 kubectl logs <pod-name> -n ecommerce-microservices
 ```

 - Si hay problemas de scheduling por CPU/memory, reduce las `requests` o aumenta la memoria de Minikube/Docker Desktop.

 ## GitHub Actions: ejecución local y adaptaciones

 El workflow principal se ubica en `.github/workflows/dev-pipeline.yml`. Está pensado para ejecutarse en un runner self-hosted Windows. Ejemplos de pasos que incluye:

 - Checkout del repo
 - Construcción de imágenes con `docker compose`
 - Carga de imágenes en Minikube (`load-images-minikube.bat`)
 - `kubectl apply -f` para desplegar
 - Verificación con `kubectl get pods`

 Importante: en PowerShell las variables de entorno se referencian como `$env:VAR`; el workflow fue ajustado para usar esa sintaxis y para invocar el `.bat` con `& .\load-images-minikube.bat`.

