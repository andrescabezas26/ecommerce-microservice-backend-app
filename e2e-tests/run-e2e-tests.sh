#!/bin/bash

# E2E Tests Runner Script for Minikube Environment
# Compatible with Git Bash on Windows and Linux/Mac

set -e

# Configuration
E2E_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULTS_DIR="$E2E_DIR/results"
NAMESPACE="ecommerce-microservices"
SERVICE_NAME="api-gateway-service"

echo "🚀 Starting E-Commerce E2E Test Suite (Minikube)"
echo "==============================================="
echo ""

# Create results directory
mkdir -p "$RESULTS_DIR"

# Check if Minikube is running
echo "🔍 Checking Minikube status..."
if ! minikube status &>/dev/null; then
    echo "💥 Minikube is not running. Please start Minikube first:"
    echo "  minikube start"
    exit 1
fi

echo "✅ Minikube is running"

# Check if services are deployed
echo "🔍 Checking if services are deployed in namespace '$NAMESPACE'..."
if ! kubectl get namespace "$NAMESPACE" &>/dev/null; then
    echo "💥 Namespace '$NAMESPACE' does not exist"
    echo "Please deploy the services first or check the namespace name"
    exit 1
fi

# List running pods
echo "📋 Current pods in namespace '$NAMESPACE':"
kubectl get pods -n "$NAMESPACE" --no-headers 2>/dev/null | while read line; do
    pod_name=$(echo "$line" | awk '{print $1}')
    pod_status=$(echo "$line" | awk '{print $3}')
    if [[ "$pod_status" == "Running" ]]; then
        echo "  ✅ $pod_name: $pod_status"
    else
        echo "  ⚠️  $pod_name: $pod_status"
    fi
done

# Check if API Gateway service exists
echo ""
echo "🔍 Checking API Gateway service..."
if ! kubectl get svc "$SERVICE_NAME" -n "$NAMESPACE" &>/dev/null; then
    echo "💥 Service '$SERVICE_NAME' not found in namespace '$NAMESPACE'"
    exit 1
fi

# Get service type
SERVICE_TYPE=$(kubectl get svc "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.type}')
echo "📌 Service type: $SERVICE_TYPE"

# Get API Gateway URL based on service type
echo "🌐 Getting API Gateway service URL..."

if [[ "$SERVICE_TYPE" == "LoadBalancer" ]] || [[ "$SERVICE_TYPE" == "NodePort" ]]; then
    # For LoadBalancer/NodePort type in Minikube
    MINIKUBE_IP=$(minikube ip 2>/dev/null)
    
    if [ -z "$MINIKUBE_IP" ]; then
        echo "💥 Could not get Minikube IP"
        exit 1
    fi
    
    NODE_PORT=$(kubectl get svc "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.ports[0].nodePort}')
    
    if [ -z "$NODE_PORT" ]; then
        echo "💥 Could not get NodePort from service"
        exit 1
    fi
    
    API_GATEWAY_URL="http://$MINIKUBE_IP:$NODE_PORT"
    
elif [[ "$SERVICE_TYPE" == "ClusterIP" ]]; then
    echo "⚠️  Service is ClusterIP. Changing to NodePort..."
    
    # Change service type to NodePort
    kubectl patch svc "$SERVICE_NAME" -n "$NAMESPACE" -p '{"spec":{"type":"NodePort"}}' &>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ Service changed to NodePort"
        
        # Wait a moment for the change to take effect
        sleep 3
        
        # Get the new NodePort
        MINIKUBE_IP=$(minikube ip 2>/dev/null)
        NODE_PORT=$(kubectl get svc "$SERVICE_NAME" -n "$NAMESPACE" -o jsonpath='{.spec.ports[0].nodePort}')
        
        API_GATEWAY_URL="http://$MINIKUBE_IP:$NODE_PORT"
    else
        echo "❌ Failed to change service type. Using port-forward instead..."
        
        # Kill any existing port-forward
        pkill -f "port-forward.*$SERVICE_NAME" 2>/dev/null || true
        
        # Start port-forward in background
        kubectl port-forward -n "$NAMESPACE" "svc/$SERVICE_NAME" 8080:8080 &>/dev/null &
        PORT_FORWARD_PID=$!
        
        echo "⏳ Waiting for port-forward to be ready..."
        sleep 5
        
        API_GATEWAY_URL="http://localhost:8080"
        
        # Set up cleanup trap
        trap "kill $PORT_FORWARD_PID 2>/dev/null || true" EXIT
    fi
fi

echo "✅ API Gateway URL: $API_GATEWAY_URL"

# Test if API Gateway is accessible
echo "🔍 Testing API Gateway connectivity..."
RETRY_COUNT=0
MAX_RETRIES=15
CONNECTED=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_GATEWAY_URL/actuator/health" 2>/dev/null || echo "000")
    
    if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "404" ]]; then
        echo "✅ API Gateway is accessible (HTTP $HTTP_CODE)"
        CONNECTED=true
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        echo "💥 API Gateway is not accessible after $MAX_RETRIES attempts"
        echo "💡 Last HTTP Code: $HTTP_CODE"
        echo "💡 Tip: Check if the API Gateway pod is running and healthy"
        echo ""
        echo "Run these commands to debug:"
        echo "  kubectl get pods -n $NAMESPACE -l app=api-gateway"
        echo "  kubectl logs -n $NAMESPACE -l app=api-gateway --tail=50"
        exit 1
    fi
    
    echo "⏳ Waiting for API Gateway to be ready... (attempt $RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

# Change to E2E directory
cd "$E2E_DIR"

# Update Minikube environment with actual URL
echo ""
echo "📝 Creating Minikube environment configuration..."

# Create temporary environment file with actual URLs
cat > minikube-environment-temp.json << EOF
{
	"id": "minikube-environment-temp",
	"name": "Minikube Environment (Temp)",
	"values": [
		{
			"key": "base_url",
			"value": "$API_GATEWAY_URL",
			"enabled": true,
			"type": "default"
		},
		{
			"key": "proxy_url",
			"value": "$API_GATEWAY_URL/app",
			"enabled": true,
			"type": "default"
		},
		{
			"key": "api_prefix",
			"value": "/app/api",
			"enabled": true,
			"type": "default"
		},
		{
			"key": "service_discovery_url",
			"value": "$API_GATEWAY_URL",
			"enabled": true,
			"type": "default"
		},
		{
			"key": "config_server_url",
			"value": "$API_GATEWAY_URL",
			"enabled": true,
			"type": "default"
		}
	],
	"_postman_variable_scope": "environment"
}
EOF

echo "✅ Environment configuration created"

# Check if newman is available
if ! command -v newman &> /dev/null; then
    if [ ! -d "node_modules" ]; then
        echo "📦 Installing Newman dependencies..."
        npm install newman newman-reporter-htmlextra --legacy-peer-deps
    fi
    NEWMAN_CMD="npx newman"
else
    NEWMAN_CMD="newman"
fi

# Run E2E tests
echo ""
echo "🧪 Running E2E tests against Minikube services..."
echo "📍 Target URL: $API_GATEWAY_URL"
echo ""

$NEWMAN_CMD run ecommerce-e2e-collection.json \
    -e minikube-environment-temp.json \
    --reporters cli,json,htmlextra \
    --reporter-json-export results/newman-report.json \
    --reporter-htmlextra-export results/newman-report.html \
    --delay-request 1000 \
    --timeout-request 15000 \
    --bail || TEST_FAILED=true

TEST_EXIT_CODE=$?

# Cleanup temporary environment file
rm -f minikube-environment-temp.json

# Kill port-forward if it was started
if [ ! -z "$PORT_FORWARD_PID" ]; then
    kill $PORT_FORWARD_PID 2>/dev/null || true
fi

# Display results
echo ""
if [ $TEST_EXIT_CODE -eq 0 ] && [ -z "$TEST_FAILED" ]; then
    echo "🎉 E2E tests completed successfully!"
    echo "📊 Test results available in: $RESULTS_DIR"
    
    # Display summary if JSON report exists
    if [ -f "$RESULTS_DIR/newman-report.json" ]; then
        echo ""
        echo "📋 Test Summary:"
        
        if command -v node &> /dev/null; then
            node -e "
                const report = require('./results/newman-report.json');
                const stats = report.run.stats;
                const assertions = stats.assertions;
                console.log('  📊 Total Requests: ' + stats.requests.total);
                console.log('  ✅ Passed: ' + (stats.requests.total - stats.requests.failed));
                console.log('  ❌ Failed: ' + stats.requests.failed);
                console.log('  🧪 Total Assertions: ' + assertions.total);
                console.log('  ✅ Assertions Passed: ' + (assertions.total - assertions.failed));
                console.log('  ❌ Assertions Failed: ' + assertions.failed);
                console.log('  ⏱️  Average Response Time: ' + Math.round(stats.requests.average) + 'ms');
            " 2>/dev/null || echo "  (Node.js required for detailed summary)"
        fi
    fi
    
    echo ""
    echo "🌐 View detailed HTML report:"
    REPORT_PATH="$(cd "$RESULTS_DIR" && pwd)/newman-report.html"
    echo "   file://$REPORT_PATH"
    
    # Try to open report automatically
    if command -v explorer.exe &> /dev/null; then
        # Windows (Git Bash)
        explorer.exe "$(cygpath -w "$REPORT_PATH")" 2>/dev/null &
    elif command -v xdg-open &> /dev/null; then
        # Linux
        xdg-open "$REPORT_PATH" &>/dev/null &
    elif command -v open &> /dev/null; then
        # macOS
        open "$REPORT_PATH" &>/dev/null &
    fi
    
    exit 0
else
    echo "💥 E2E tests failed!"
    echo "📊 Check test results in: $RESULTS_DIR"
    
    REPORT_PATH="$(cd "$RESULTS_DIR" && pwd)/newman-report.html"
    echo "🌐 View detailed report: file://$REPORT_PATH"
    
    exit 1
fi