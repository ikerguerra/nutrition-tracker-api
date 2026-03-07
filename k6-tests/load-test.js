import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 Options: define the load testing stages
export const options = {
    stages: [
        { duration: '30s', target: 20 }, // Ramp-up to 20 users over 30 seconds
        { duration: '1m', target: 20 },  // Stay at 20 users for 1 minute
        { duration: '30s', target: 0 },  // Ramp-down to 0 users over 30 seconds
    ],
    thresholds: {
        // We want the 95th percentile response time to be < 200ms
        http_req_duration: ['p(95)<200'],
        // We want the error rate to be < 1%
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://host.docker.internal:8080/api/v1';

export default function () {
    // 1. Test standard health endpoint (replace with your actual health check if different)
    const healthRes = http.get('http://host.docker.internal:8080/actuator/health');
    check(healthRes, {
        'health is status 200': (r) => r.status === 200,
    });

    // 2. Here you can add tests targeting your controllers directly
    // This JWT token must be updated with a real one copied from your logged in session (PWA/Postman).
    // You can get it directly from your Chrome DevTools -> Application -> Local Storage (or Network tab Authorization header)
    const TOKEN = '<PON_TU_TOKEN_AQUI>';

    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json',
        },
    };

    // Attack a real endpoint from the FoodController that requires @AuthenticationPrincipal
    const foodFavoritesRes = http.get(`${BASE_URL}/foods/favorites`, params);

    check(foodFavoritesRes, {
        'favorites list is status 200': (r) => r.status === 200,
        'favorites list is fast': (r) => r.timings.duration < 200
    });

    // Sleep between iterations to simulate realistic user behavior (1 second)
    sleep(1);
}
