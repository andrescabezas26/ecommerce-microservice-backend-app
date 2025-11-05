 # ecommerce-microservice-backend-app

Este repositorio contiene una arquitectura de microservicios Spring Boot completamente automatizada para pruebas unitarias, integración, E2E y despliegue en Kubernetes (Minikube). Todos los pipelines CI/CD se ejecutan mediante un runner de GitHub Actions local.

## 🎯 Resumen de lo realizado

### Arquitectura de Microservicios
- **5 microservicios principales**: user-service, order-service, product-service, payment-service, shipping-service
- **Servicios de infraestructura**: service-discovery (Eureka), cloud-config (Config Server), api-gateway, proxy-client, favourite-service
- **Trazabilidad distribuida**: Zipkin integrado para observabilidad

### Testing Implementado
- ✅ **25 pruebas unitarias** (5 por cada microservicio principal)
- ✅ **Pruebas de integración** para validar comunicación entre servicios
- ✅ **Pruebas E2E** contra servicios desplegados en Minikube (Newman/Postman)
- ⏭️ **Pruebas de rendimiento**: Pendiente (fuera del alcance actual)

### Pipelines CI/CD Automatizados
- **dev-pipeline.yml**: Construcción + pruebas unitarias (triggered en push/PR)
- **stage-pipeline.yml**: Integración + deploy a Minikube + E2E (triggered tras éxito de dev)
- **Lógica inteligente de Minikube**: Detecta despliegues existentes y reutiliza sin reconstruir

### Scripts de Automatización
- `build-images-minikube.ps1` — Construye imágenes **directamente en Minikube** (Maven + Docker)
- `deploy-individual-services.bat` — Despliega servicios en Kubernetes en orden correcto
- `cleanup-services.bat` — Limpia namespace y recursos


---

## 📋 Requisitos Previos

Antes de ejecutar cualquier pipeline, asegúrate de tener instalado y funcional:

### Software Requerido
- **Windows 10/11** (recomendado; los scripts están en PowerShell)
- **Docker Desktop** (versión reciente, con soporte para Minikube)
- **Minikube** (última versión; [descargar aquí](https://minikube.sigs.k8s.io/docs/start/))
- **kubectl** (instalado automáticamente con Minikube)
- **Git** (para clonar el repo)
- **JDK 11** (para compilar los microservicios con Maven)
- **Node.js 18+** y npm (para tests E2E con Newman)
- **Maven** (puede estar embebido en cada carpeta via `mvnw.cmd`)
- **PowerShell 5.1+** (viene con Windows; ejecutar como Administrador)

### Verificar Instalación
```powershell
# Verificar Docker
docker --version
docker run hello-world

# Verificar Minikube
minikube version

# Verificar kubectl
kubectl version --client

# Verificar JDK
java -version

# Verificar Node
node --version
npm --version
```

---

## 🔧 Configuración Inicial

### 1. Clonar el Repositorio
```powershell
git clone https://github.com/andrescabezas26/ecommerce-microservice-backend-app.git
cd ecommerce-microservice-backend-app
```

### 2. Levantar Minikube (si no está corriendo)
```powershell
# Verificar estado
minikube status

# Si no está corriendo, iniciar
minikube start --memory=12974 --cpus=4 --driver=docker

# Verificar que está activo
minikube status
# Esperado:
# minikube
# type: Control Plane
# host: Running
# kubelet: Running
# apiserver: Running
```

**Nota**: Los valores `--memory=12974` (~13 GB) y `--cpus=4` son recomendados. Ajusta según tu máquina.

### 3. Configurar GitHub Actions Runner Local
Un "runner" es un agente que ejecuta los workflows de GitHub en tu máquina local.

#### Paso 1: Descargar el runner
```powershell
# Crear carpeta para el runner
mkdir C:\actions-runner
cd C:\actions-runner

# Descargar runner (Windows x64)
Invoke-WebRequest -Uri "https://github.com/actions/runner/releases/download/v2.310.0/actions-runner-win-x64-2.310.0.zip" -OutFile "actions-runner-win-x64-2.310.0.zip"

# Descomprimir
Expand-Archive -Path "actions-runner-win-x64-2.310.0.zip" -DestinationPath .
```

#### Paso 2: Registrar el runner
```powershell
cd C:\actions-runner

# Ejecutar el configurador
.\config.cmd `
  --url "https://github.com/andrescabezas26/ecommerce-microservice-backend-app" `
  --token "<GITHUB_TOKEN>" `
  --name "minikube-runner-local" `
  --runnergroup "Default" `
  --labels "self-hosted,windows,minikube" `
  --work "_work"
```

**Obtener `<GITHUB_TOKEN>`**:
1. Ve a: Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Crea un nuevo token con permisos: `repo`, `workflow`
3. Copia el token y úsalo arriba

#### Paso 3: Instalar como servicio (opcional pero recomendado)
```powershell
cd C:\actions-runner

# Ejecutar como administrador
.\config.cmd --url "https://github.com/andrescabezas26/ecommerce-microservice-backend-app" --token "<GITHUB_TOKEN>" --name "minikube-runner" --runnergroup "Default" --labels "self-hosted,windows,minikube"

# Instalar como servicio Windows
.\svc.cmd install

# Iniciar servicio
.\svc.cmd start
```

Para verificar que está corriendo:
```powershell
# Ver servicios
Get-Service "GitHub Actions Runner"

# Si necesitas detenerlo
.\svc.cmd stop
```

**Si prefieres ejecutar manualmente** (sin servicio):
```powershell
cd C:\actions-runner
.\run.cmd
```

---

## 🚀 Flujo de Ejecución de Pipelines

### Arquitectura de Pipelines

```
┌─────────────────────────────────────────────────────────────┐
│ Evento: git push origin master                              │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ dev-pipeline.yml (Ejecuta en Runner Local)                  │
│ ✓ Setup JDK 11 + Maven                                      │
│ ✓ Run Unit Tests (todas los servicios)                      │
│ ✓ Build Docker images (docker compose)                      │
│ ✓ List images                                               │
│ ✓ Trigger stage-pipeline si SUCCESS                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
                    SUCCESS? (Si)
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ stage-pipeline.yml (Ejecuta en Runner Local)                │
│ 1. Check Minikube + Deployed Services                       │
│    → Si no existen, crea Minikube                           │
│    → Si existen, reutiliza (ahorra tiempo)                  │
│                                                              │
│ 2. Build images directamente en Minikube                    │
│    build-images-minikube.ps1:                               │
│    - Maven compile (mvn clean package -DskipTests)          │
│    - Docker build (dentro de Minikube)                      │
│                                                              │
│ 3. Deploy servicios en Kubernetes                           │
│    deploy-individual-services.bat:                          │
│    - Crea namespace ecommerce-microservices                 │
│    - Aplica YAML en orden correcto                          │
│                                                              │
│ 4. Wait for pods to be ready (readiness checks)             │
│                                                              │
│ 5. Run Integration Tests                                    │
│    (contra servicios desplegados)                           │
│                                                              │
│ 6. Run E2E Tests (npm run test:e2e:minikube)                │
│    (Newman contra API Gateway)                              │
│                                                              │
│ 7. Generate Stage Report                                    │
└─────────────────────────────────────────────────────────────┘
```

### Paso a Paso: Ejecutar los Pipelines

#### **Opción A: Automática (Recomendada)**

**Terminal 1: Inicia el runner** (mantenerla abierta)
```powershell
cd C:\actions-runner
.\run.cmd
```

Verás algo como:
```
Connected to GitHub

Listening for Jobs
```

**Terminal 2: Realiza un push** (dispara dev-pipeline automáticamente)
```powershell
cd C:\Universidad\Semestre VIII\Ingesoft V\ecommerce-microservice-backend-app

# Hacer un cambio (ej. actualizar README)
git add .
git commit -m "trigger: update pipeline"
git push origin master
```

**Observar ejecución en GitHub**:
1. Ve a: https://github.com/andrescabezas26/ecommerce-microservice-backend-app/actions
2. Verás "dev-pipeline" en ejecución
3. Terminal 1 mostrará logs en vivo
4. Tras completarse, automáticamente se dispara "stage-pipeline"

#### **Opción B: Manual (workflow_dispatch)**

1. Ve a: GitHub → Actions → "Stage Pipeline - Integration Tests & Deploy"
2. Click en "Run workflow"
3. Opcionalmente, marca: `force_recreate_minikube = true` (para recrear Minikube completamente)
4. Click "Run workflow"
5. En Terminal, inicia el runner:
   ```powershell
   cd C:\actions-runner
   .\run.cmd
   ```

---

## 🔌 Verificar Servicios Desplegados

Una vez que el stage-pipeline completa, los servicios están corriendo en Minikube. Para acceder a ellos:

### 1. Verificar Estado de Pods
```powershell
kubectl get pods -n ecommerce-microservices -o wide

# Esperado:
# NAME                                  READY   STATUS    RESTARTS   AGE
# api-gateway-xxxxx                     1/1     Running   0          2m
# user-service-xxxxx                    1/1     Running   0          2m
# order-service-xxxxx                   1/1     Running   0          2m
# payment-service-xxxxx                 1/1     Running   0          2m
# ... etc
```

### 2. Verificar Servicios
```powershell
kubectl get svc -n ecommerce-microservices

# Esperado:
# NAME                    TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)
# api-gateway-service     ClusterIP   10.96.1.100     <none>        8080/TCP
# user-service            ClusterIP   10.96.1.101     <none>        8700/TCP
# ... etc
```

### 3. Port-Forward al API Gateway (IMPORTANTE)

Abre una **nueva terminal PowerShell**:
```powershell
kubectl port-forward svc/api-gateway-service 8080:8080 -n ecommerce-microservices

# Verás:
# Forwarding from 127.0.0.1:8080 -> 8080
# Forwarding from [::1]:8080 -> 8080
# Waiting for connections ...
```

**Mantén esta terminal abierta mientras accedas al API Gateway**.

### 4. Probar Acceso a API Gateway
En **otra terminal**:
```powershell
# Health check del API Gateway
curl http://localhost:8080/actuator/health

# Esperado:
# {"status":"UP"}
```

### 5. Port-Forward a Zipkin (Opcional, para observabilidad)
```powershell
kubectl port-forward svc/zipkin-service 9411:9411 -n ecommerce-microservices

# Luego acceder a: http://localhost:9411
```

### 6. Ver Logs de un Pod
```powershell
# Logs en vivo de un servicio
kubectl logs -f deployment/user-service -n ecommerce-microservices

# Últimas 50 líneas
kubectl logs --tail=50 deployment/user-service -n ecommerce-microservices
```

---

## 🧪 Ejecutar Tests Manualmente

### Tests Unitarios (localmente, sin Minikube)
```powershell
cd user-service
mvn test -Dtest=UserServiceTest
cd ..
```

### Tests de Integración (requiere servicios en Minikube)
```powershell
# Asegúrate de que pods estén corriendo
kubectl get pods -n ecommerce-microservices

# Ejecutar integración
mvn test -Dtest=*IntegrationTest -Dspring.profiles.active=test
```

### Tests E2E (requiere API Gateway con port-forward en 8080)
```powershell
cd e2e-tests

# Instalar dependencias
npm install --legacy-peer-deps

# Ejecutar contra Minikube
npm run test:e2e:minikube

# Ver reporte HTML generado
e2e-tests/results/newman-report.html
```

---

## 📝 Descripciones de Scripts

### `build-images-minikube.ps1`
**Qué hace**: Construye imágenes Docker **directamente dentro del daemon de Minikube** (no en Docker Desktop).

```powershell
.\build-images-minikube.ps1
```

**Pasos internos**:
1. Configura Docker para usar Minikube (`minikube docker-env`)
2. Para cada servicio (user-service, product-service, etc.):
   - Compila con Maven: `mvn clean package -DskipTests`
   - Construye imagen: `docker build -t service-name:latest .`
3. Verifica imágenes creadas en Minikube

**Ventaja**: Las imágenes se crean directamente en Minikube, sin necesidad de transferencias externas.

### `deploy-individual-services.bat`
**Qué hace**: Despliega servicios en Kubernetes en el orden correcto de dependencias.

```cmd
.\deploy-individual-services.bat
```

**Orden de despliegue**:
1. Namespace ecommerce-microservices
2. Zipkin (infraestructura)
3. Service Discovery (Eureka)
4. Cloud Config Server
5. Otros servicios (user, order, product, payment, shipping, favourite)
6. API Gateway
7. Proxy Client

**Nota**: El script respeta dependencias; por ejemplo, cloud-config se despliega antes que los servicios de negocio.

### `cleanup-services.bat`
**Qué hace**: Limpia completamente el namespace y recursos.

```cmd
.\cleanup-services.bat
```

---

## 📚 Archivos Relevantes

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

 8) Ejecutar el GitHub Actions runner local (PowerShell):

 ```powershell
 PS C:\actions-runner> .\run.cmd
 ```

 ## Despliegue Rápido - Limpiar y Redeplegar Microservicios

 ### 1. Eliminar el namespace anterior (limpiar todo):
 ```powershell
 kubectl delete namespace ecommerce-microservices
 ```

 ### 2. Compilar e construir todas las imágenes en Minikube:
 ```powershell
 .\build-images-minikube.ps1
 ```

 Este script:
 - Configura Docker para usar el daemon de Minikube
 - Compila cada servicio con Maven (`mvn clean package -DskipTests`)
 - Construye las imágenes Docker en Minikube
 - Etiqueta con `latest` para siempre usar la versión más reciente

 ### 3. Desplegar todos los servicios en Kubernetes:
 ```powershell
 .\deploy-individual-services.bat
 ```

 Este script aplica los manifiestos YAML en el orden correcto de dependencias.

 ### 4. Verificar que los pods están corriendo:
 ```powershell
 kubectl get pods -n ecommerce-microservices -o wide
 ```

 ### 5. Habilitar port-forward al API Gateway (en otra terminal PowerShell):
 ```powershell
 kubectl port-forward svc/api-gateway-service 8080:8080 -n ecommerce-microservices
 ```

 Luego puedes acceder en: `http://localhost:8080`

 ### 6. Habilitar port-forward a Zipkin (en otra terminal PowerShell):
 ```powershell
 kubectl port-forward svc/zipkin-service 9411:9411 -n ecommerce-microservices
 ```

 Luego puedes acceder en: `http://localhost:9411`

 ### 7. Actualizar un servicio específico (ej. user-service):
 ```powershell
 .\build-images-minikube.ps1 -Service user-service
 ```

 O manualmente:
 ```powershell
 & minikube docker-env --shell powershell | Invoke-Expression
 cd user-service
 mvn clean package -DskipTests -q
 docker build -t 'user-service:latest' .
 cd ..
 kubectl rollout restart deployment/user-service -n ecommerce-microservices
 kubectl rollout status deployment/user-service -n ecommerce-microservices --timeout=120s
 ```

 ## Pipelines CI/CD

 ### 1. Development Pipeline (`dev-pipeline.yml`)
 **Propósito**: Construcción y testing unitario
 **Triggers**: Push y Pull Request a main/master
 **Pasos**:
 - Setup JDK 11 y cache Maven
 - Ejecuta unit tests en todos los microservicios
 - Construye imágenes Docker
 - Lista imágenes construidas

 ### 2. Stage Pipeline (`stage-pipeline.yml`)
 **Propósito**: Testing de integración y despliegue a entorno de stage
 **Triggers**: 
 - Automático tras éxito del dev-pipeline
 - Manual con opción de recrear Minikube
 **Pasos**:
 - Ejecuta integration tests
 - **Gestión inteligente de Minikube**:
   - Si existe: limpia namespace e imágenes anteriores
   - Si no existe: crea cluster con `minikube start --memory=12974 --cpus=4 --driver=docker`
 - Construye imágenes frescas
 - Carga imágenes en Minikube
 - Despliega en Kubernetes
 - Ejecuta E2E tests (placeholder)
 - Genera reporte de stage

 ### Ejecución de Pipelines
 ```powershell
 # 1. Asegúrate de que el runner esté corriendo
 PS C:\actions-runner> .\run.cmd

 # 2. Push para activar dev-pipeline (automático)
 git push origin master

 # 3. Stage-pipeline se ejecutará automáticamente tras dev-pipeline
 # O ejecutar manualmente desde GitHub Actions web interface
 ```

 ## Detalles y notas prácticas

 - Puertos y sondas: varias aplicaciones arrancan en puertos distintos (por ejemplo `api-gateway` en 8080 y `user-service` en 8700). Verifica la configuración del `containerPort`, `readinessProbe` y `livenessProbe` para cada deployment.
 - Si un pod entra en `CrashLoopBackOff`, revisa los logs con:

 ```powershell
 kubectl logs <pod-name> -n ecommerce-microservices
 ```

 - Si hay problemas de scheduling por CPU/memory, reduce las `requests` o aumenta la memoria de Minikube/Docker Desktop.

---

## ⚠️ Troubleshooting

### Error: "Minikube is not running"
**Solución**:
```powershell
minikube start --memory=12974 --cpus=4 --driver=docker
minikube status
```

### Error: "No space left on device" (Docker/Minikube lleno)
**Solución**:
```powershell
# Limpiar imágenes dangling
docker system prune -a --volumes

# O recrear Minikube completamente
minikube delete
minikube start --memory=12974 --cpus=4 --driver=docker
```

### Error: "Unable to connect to the server"
**Causa**: Minikube no está accesible o kubectl no está configurado.
```powershell
# Verifica que Minikube esté corriendo
minikube status

# Reinicia kubectl context
kubectl config current-context
kubectl config set-context minikube
kubectl cluster-info
```

### Error: "ImagePullBackOff" en pods
**Causa**: Las imágenes no se construyeron correctamente en Minikube.
```powershell
# Verificar imágenes en Minikube
minikube ssh
docker images | grep ecommerce
exit

# Si faltan imágenes, reconstruir
.\build-images-minikube.ps1
```

### Error: "CrashLoopBackOff" en un pod de servicio
**Solución**:
```powershell
# Ver logs del pod
kubectl logs -f deployment/user-service -n ecommerce-microservices --tail=100

# Describir el pod para más detalles
kubectl describe pod <pod-name> -n ecommerce-microservices

# Posibles causas: servicio de config no disponible, BD no conectada, puerto en uso
```

### Error: "Port 8080 already in use"
**Solución**:
```powershell
# Encontrar proceso usando el puerto
netstat -ano | findstr :8080

# Matar el proceso (reemplaza PID)
taskkill /PID <PID> /F

# O cambiar puerto en port-forward
kubectl port-forward svc/api-gateway-service 9090:8080 -n ecommerce-microservices
```

### Error: "kubectl: command not found" en PowerShell
**Solución**:
```powershell
# kubectl debería venir con Minikube
# Reinicia PowerShell y verifica PATH

# O agrega Minikube al PATH manualmente
$env:PATH += ";C:\Users\<tu-usuario>\.minikube\bin"

# Verifica
kubectl version --client
```

### Error: "Tests E2E fallan con status 400/500"
**Causa**: Servicios no están listos o variables de entorno incorrectas.
```powershell
# 1. Verificar que API Gateway está accesible
curl http://localhost:8080/actuator/health

# 2. Verificar que todos los pods estén Running
kubectl get pods -n ecommerce-microservices

# 3. Revisar logs del API Gateway
kubectl logs -f deployment/api-gateway -n ecommerce-microservices --tail=50

# 4. Verificar configuración de minikube-environment.json (debe tener baseUrl correcta)
cat e2e-tests/minikube-environment.json
```

### Error: "Runner no se conecta a GitHub"
**Solución**:
```powershell
# Verificar que el runner está corriendo
cd C:\actions-runner
.\run.cmd

# Si cambiaste token, reconfigura
.\config.cmd --url "https://github.com/andrescabezas26/ecommerce-microservice-backend-app" --token "<NUEVO_TOKEN>"

# Si el runner no aparece en GitHub, verifica logs
type _diag\Runner_*.log
```

---

## ❓ Preguntas Frecuentes (FAQ)

### ¿Cuánto tiempo tarda el pipeline completo?
- **dev-pipeline**: ~5-10 minutos (build + unit tests)
- **stage-pipeline**: 
  - Primera ejecución (sin Minikube): ~15-20 minutos (inicializa Minikube, despliega, integración, E2E)
  - Ejecuciones posteriores (con Minikube corriendo): ~3-5 minutos (reutiliza deployments, solo tests)

### ¿Puedo ejecutar solo integration tests sin E2E?
Sí, en el runner ejecuta:
```powershell
mvn test -Dtest=*IntegrationTest -Dspring.profiles.active=test
```

### ¿Qué es "servicios_deployed" en el stage-pipeline?
Es una variable que indica si los servicios ya están desplegados en Minikube. Si es `true`, el pipeline no reconstruye (ahorra tiempo); si es `false`, construye e despliega desde cero.

### ¿Cómo agrego un nuevo microservicio?
1. Crea carpeta: `new-service/`
2. Genera Spring Boot project dentro
3. Crea Dockerfile en la carpeta raíz
4. Agrega manifiesto en `k8s/XX-new-service.yaml`
5. Actualiza `build-images-minikube.ps1` para incluir el nuevo servicio
6. Actualiza `deploy-individual-services.bat` con kubectl apply

### ¿Puedo acceder a Zipkin para observabilidad?
Sí, una vez que los servicios estén corriendo:
```powershell
kubectl port-forward svc/zipkin-service 9411:9411 -n ecommerce-microservices

# Luego acceder a
# http://localhost:9411
```

### ¿Cómo cambio el número de replicas de un servicio?
Edita el manifiesto correspondiente en `k8s/XX-service.yaml` (busca `replicas: 1`) y redespliega:
```powershell
kubectl apply -f k8s/XX-service.yaml
```

### ¿Qué pasa si quiero ejecutar tests en una rama diferente?
El pipeline se dispara automáticamente en cualquier push. Solo haz:
```powershell
git checkout -b feature/my-feature
# ... haz cambios ...
git push origin feature/my-feature
```

### ¿Cómo veo los logs de un servicio en tiempo real?
```powershell
kubectl logs -f deployment/<service-name> -n ecommerce-microservices
```

### ¿Puedo resetear completamente Minikube?
```powershell
minikube delete
minikube start --memory=12974 --cpus=4 --driver=docker
```

---

## 📖 Referencias y Documentación

- [Minikube Official Docs](https://minikube.sigs.k8s.io/)
- [kubectl Cheatsheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Spring Boot Microservices](https://spring.io/projects/spring-cloud)
- [GitHub Actions Self-hosted Runners](https://docs.github.com/en/actions/hosting-your-own-runners)
- [Newman (Postman CLI)](https://github.com/postmanlabs/newman)
- [Docker Official Docs](https://docs.docker.com/)

---

## 📞 Contacto y Soporte

Si encuentras problemas o tienes sugerencias:
1. Revisa los logs del pipeline en GitHub Actions
2. Consulta la sección de Troubleshooting arriba
3. Abre un issue en el repositorio con detalles de tu entorno


