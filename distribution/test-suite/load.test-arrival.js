import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:2020';

const RATE = Number(__ENV.RATE || 100);

export const options = {
    scenarios: {
        fixed_rate: {
            executor: 'constant-arrival-rate',
            rate: RATE,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 200,
            maxVUs: 1000,
        },
    },

    discardResponseBodies: true,

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
        dropped_iterations: ['count==0'],
    },
};

export default function () {
    const response = http.get(`${BASE_URL}/`);

    check(response, {
        'status is 200': (r) => r.status === 200,
    });
}