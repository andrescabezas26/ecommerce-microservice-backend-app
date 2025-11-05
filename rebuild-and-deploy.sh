#!/bin/bash

# Script para reconstruir y redesplegar un microservicio en Minikube
# Uso: ./rebuild-and-deploy.sh proxy-client

set -e

SERVICE=${1:-proxy-client}
NAMESPACE="ecommerce-microservices"
IMAGE_TAG="v1.0.$(date +%Y%m%d%H%M%S)"

echo "🔨 Rebuilding and redeploying: $SERVICE"
echo "============================================"

# Validate service exists
if [ ! -d "$SERVICE" ]; then
    echo "❌ Directory '$SERVICE' not found"
    exit 1
fi

echo ""
echo "1️⃣  Configuring Minikube Docker environment..."
eval $(minikube docker-env)
echo "   ✅ Docker environment configured"

echo ""
echo "2️⃣  Building Maven project..."
cd "$SERVICE"
mvn clean package -DskipTests -q
echo "   ✅ Maven build successful"

echo ""
echo "3️⃣  Building Docker image with tag: $IMAGE_TAG..."
docker build -t "${SERVICE}:${IMAGE_TAG}" -t "${SERVICE}:latest" .
echo "   ✅ Docker image built successfully"

cd ..

echo ""
echo "4️⃣  Updating Kubernetes deployment..."

# Check if deployment exists
if ! kubectl get deployment "$SERVICE" -n "$NAMESPACE" &>/dev/null; then
    echo "   ❌ Deployment '$SERVICE' not found in namespace '$NAMESPACE'"
    exit 1
fi

# Update image
echo "   📝 Updating image to: ${SERVICE}:${IMAGE_TAG}"
kubectl set image deployment/"$SERVICE" \
    "$SERVICE"="${SERVICE}:${IMAGE_TAG}" \
    -n "$NAMESPACE"

# Set imagePullPolicy
echo "   📝 Setting imagePullPolicy to IfNotPresent..."
kubectl patch deployment "$SERVICE" -n "$NAMESPACE" -p '{
    "spec": {
        "template": {
            "spec": {
                "containers": [{
                    "name": "'"$SERVICE"'",
                    "imagePullPolicy": "IfNotPresent"
                }]
            }
        }
    }
}'

echo "   ✅ Deployment updated"

echo ""
echo "5️⃣  Waiting for rollout to complete..."
if kubectl rollout status deployment/"$SERVICE" -n "$NAMESPACE" --timeout=180s; then
    echo "   ✅ Rollout completed successfully"
else
    echo "   ⚠️  Rollout timed out or failed"
    echo ""
    echo "   Checking pod status..."
    kubectl get pods -n "$NAMESPACE" -l app="$SERVICE"
    
    echo ""
    echo "   Recent logs:"
    kubectl logs -n "$NAMESPACE" -l app="$SERVICE" --tail=30
    exit 1
fi

echo ""
echo "6️⃣  Verifying deployment..."
POD_STATUS=$(kubectl get pods -n "$NAMESPACE" -l app="$SERVICE" -o jsonpath='{.items[0].status.phase}')

if [ "$POD_STATUS" == "Running" ]; then
    echo "   ✅ Pod is running"
    
    echo ""
    echo "📋 Pod Information:"
    kubectl get pods -n "$NAMESPACE" -l app="$SERVICE" -o wide
    
    echo ""
    echo "📝 Recent logs (last 20 lines):"
    kubectl logs -n "$NAMESPACE" -l app="$SERVICE" --tail=20
    
    echo ""
    echo "✅ Deployment completed successfully!"
    echo "🎉 Service '$SERVICE' is now running with the latest code"
else
    echo "   ⚠️  Pod status: $POD_STATUS"
    echo ""
    echo "📝 Pod logs:"
    kubectl logs -n "$NAMESPACE" -l app="$SERVICE" --tail=50
    exit 1
fi

echo ""
echo "💡 Tips:"
echo "   - View logs: kubectl logs -n $NAMESPACE -l app=$SERVICE -f"
echo "   - Describe pod: kubectl describe pod -n $NAMESPACE -l app=$SERVICE"
echo "   - Check service: kubectl get svc -n $NAMESPACE ${SERVICE}-service"