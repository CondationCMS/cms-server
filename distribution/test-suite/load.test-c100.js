import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:2020';

export const options = {
    scenarios: {
        concurrency_test: {
            executor: 'constant-vus',
            vus: 100,
            duration: '2m',
        },
    },

    discardResponseBodies: true,

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
    },
};

export default function () {
    const response = http.get(`${BASE_URL}/`);

    check(response, {
        'status is 200': (r) => r.status === 200,
    });
}