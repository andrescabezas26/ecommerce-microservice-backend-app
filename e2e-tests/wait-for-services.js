const axios = require('axios');

const SERVICES = [
    { name: 'API Gateway', url: 'http://localhost:8080/actuator/health' },
    { name: 'Service Discovery', url: 'http://localhost:8761/actuator/health' },
    { name: 'Config Server', url: 'http://localhost:8888/actuator/health' },
    { name: 'Proxy Client', url: 'http://localhost:9191/actuator/health' }
];

const MAX_RETRIES = 30;
const RETRY_DELAY = 5000; // 5 seconds

async function waitForService(service) {
    console.log(`⏳ Waiting for ${service.name}...`);
    
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
        try {
            const response = await axios.get(service.url, { 
                timeout: 3000,
                validateStatus: (status) => status < 500 // Accept 2xx, 3xx, 4xx
            });
            
            console.log(`✅ ${service.name} is ready (attempt ${attempt})`);
            return true;
        } catch (error) {
            console.log(`❌ ${service.name} not ready (attempt ${attempt}/${MAX_RETRIES}): ${error.message}`);
            
            if (attempt === MAX_RETRIES) {
                console.error(`💥 ${service.name} failed to start after ${MAX_RETRIES} attempts`);
                return false;
            }
            
            console.log(`⏳ Retrying in ${RETRY_DELAY/1000} seconds...`);
            await new Promise(resolve => setTimeout(resolve, RETRY_DELAY));
        }
    }
    return false;
}

async function waitForAllServices() {
    console.log('🚀 Starting service health checks...\n');
    
    const results = await Promise.all(
        SERVICES.map(service => waitForService(service))
    );
    
    const allReady = results.every(result => result === true);
    
    if (allReady) {
        console.log('\n🎉 All services are ready! Starting E2E tests...\n');
        process.exit(0);
    } else {
        console.log('\n💥 Some services failed to start. E2E tests cannot proceed.\n');
        process.exit(1);
    }
}

// Handle graceful shutdown
process.on('SIGINT', () => {
    console.log('\n⚠️  Service health check interrupted');
    process.exit(1);
});

process.on('SIGTERM', () => {
    console.log('\n⚠️  Service health check terminated');
    process.exit(1);
});

waitForAllServices().catch(error => {
    console.error('💥 Unexpected error during service health checks:', error);
    process.exit(1);
});