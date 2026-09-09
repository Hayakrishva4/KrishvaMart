# Load Testing (Section 9)

Requirement: minimum 10 concurrent users, 60 seconds, against the browse
endpoint (the highest-traffic, unauthenticated path).

## Option A: Apache Bench (quickest)

```bash
# 10 concurrent users hammering product search for 60 seconds
ab -c 10 -t 60 http://localhost:8080/krishvamart/api/v1/products
```

Record the results (requests/sec, mean latency, failed requests) in this
file or a linked results doc before the Full Build + Deploy checkpoint.

## Option B: JMeter (matches the spec's other suggested tool)

A minimal test plan is provided at `docs/load-test-plan.jmx`. Run it with:

```bash
jmeter -n -t docs/load-test-plan.jmx -l docs/load-test-results.jtl \
  -Jhost=localhost -Jport=8080 -Jpath=/krishvamart/api/v1/products
```

It spins up a thread group of 10 users, each looping product-search requests
for 60 seconds (ramp-up 5s), matching the Section 9 minimum.

## Results log

| Date | Environment | Concurrent users | Duration | Requests | Failures | Mean latency | Notes |
|---|---|---|---|---|---|---|---|
| _(fill in before Sep 21)_ | | 10 | 60s | | | | |
