@echo off
echo Eliminando todos los microservicios...

echo.
echo Eliminando deployments y servicios...
kubectl delete -f k8s\11-favourite-service.yaml
kubectl delete -f k8s\10-shipping-service.yaml
kubectl delete -f k8s\09-product-service.yaml
kubectl delete -f k8s\08-payment-service.yaml
kubectl delete -f k8s\07-order-service.yaml
kubectl delete -f k8s\06-proxy-client.yaml
kubectl delete -f k8s\05-api-gateway.yaml
kubectl delete -f k8s\04-user-service.yaml
kubectl delete -f k8s\03-cloud-config.yaml
kubectl delete -f k8s\02-service-discovery.yaml
kubectl delete -f k8s\01-zipkin.yaml

echo.
echo Verificando que todos los pods han sido eliminados...
kubectl get pods -n ecommerce-microservices

echo.
echo Opcional: eliminar el namespace (descomenta la línea siguiente si quieres eliminar todo)
kubectl delete namespace ecommerce-microservices

echo.
echo Cleanup completado!