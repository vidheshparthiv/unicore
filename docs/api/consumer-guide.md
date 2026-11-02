# UniCore Consumer Guide

This guide describes how to integrate UniCore services into your Java application.

---

## 1. Adding the Dependency

Include the UniCore dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.unicore</groupId>
    <artifactId>unicore</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 2. Choosing the Right Service

| If your requirement is... | Use UniCore Service | Primary Package |
|---|---|---|
| Order batch or async jobs deterministically | `Scheduler` | `com.unicore.api.scheduler` |
| Execute tasks with bounded workers & queues | `ExecutionService` | `com.unicore.api.execution` |
| Cache data with explicit capacity & TTL | `UniCache` | `com.unicore.api.cache` |
| Protect APIs from excess request rates | `RateLimiter` | `com.unicore.api.traffic` |
| Execute operations with retry & circuit protection | `ResilienceEngine` | `com.unicore.api.resilience` |
| Allocate shared JVM permits without deadlocks | `ResourceManager` | `com.unicore.api.resource` |
| Distribute traffic across worker nodes | `LoadBalancer` | `com.unicore.api.loadbalance` |

---

## 3. Best Practices for Production

1. **Always Close Resources:** Services implementing `AutoCloseable` should be closed during application shutdown to terminate background threads cleanly.
2. **Handle Rejections Explicitly:** When queues or rate limiters reject requests, handle fallback paths gracefully.
3. **Inspect Metrics:** Periodically extract `MetricsSnapshot` to monitor latency histograms and throughput.
