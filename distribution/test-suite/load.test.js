import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:2020';

export const options = {
    discardResponseBodies: true,

    stages: [
        { duration: '30s', target: 20 },
        { duration: '30s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: [
            'p(95)<500',
            'p(99)<1000',
        ],
        checks: ['rate>0.99'],
    },
};

export default function () {
    group('CMS Base Page Performance', () => {
        const response = http.get(`${BASE_URL}/`, {
            tags: {
                endpoint: 'homepage',
            },
        });

        check(response, {
            'status is 200': (r) => r.status === 200,
            'content type is HTML': (r) =>
                r.headers['Content-Type']?.includes('text/html'),
        });
    });

    sleep(1);
}