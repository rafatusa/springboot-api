import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('error_rate');
const responseTime = new Trend('response_time');

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // ramp up to 10 VUs
    { duration: '60s', target: 50 },   // ramp up to 50 VUs
    { duration: '60s', target: 50 },   // hold at 50 VUs
    { duration: '30s', target: 0 },    // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95th percentile < 500ms
    error_rate: ['rate<0.01'],          // error rate < 1%
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const params = {
    headers: { 'Content-Type': 'application/json' },
    timeout: '10s',
  };

  // Test: list items
  const listRes = http.get(`${BASE_URL}/api/v1/items`, params);
  check(listRes, {
    'list items status 200': (r) => r.status === 200,
    'list items has data': (r) => r.json('success') === true,
  });
  errorRate.add(listRes.status !== 200);
  responseTime.add(listRes.timings.duration);

  sleep(0.5);

  // Test: health check
  const healthRes = http.get(`${BASE_URL}/actuator/health`, params);
  check(healthRes, {
    'health status 200': (r) => r.status === 200,
  });
  errorRate.add(healthRes.status !== 200);

  sleep(0.5);

  // Test: get single item
  const itemRes = http.get(`${BASE_URL}/api/v1/items/1`, params);
  check(itemRes, {
    'get item status 200': (r) => r.status === 200,
    'item has name': (r) => r.json('data.name') !== null,
  });
  errorRate.add(itemRes.status !== 200);
  responseTime.add(itemRes.timings.duration);

  sleep(1);
}

export function handleSummary(data) {
  return {
    stdout: JSON.stringify({
      p95: data.metrics.http_req_duration.values['p(95)'],
      p99: data.metrics.http_req_duration.values['p(99)'],
      rps: data.metrics.http_reqs.values.rate,
      errorRate: data.metrics.http_req_failed.values.rate,
    }, null, 2),
  };
}
