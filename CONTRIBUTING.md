# Contributing to UniCore

Thank you for your interest in contributing to **UniCore**! We are building a unified, production-grade Java systems library that converts OS, concurrency, and distributed algorithms into robust, reusable runtime components.

---

## 📜 Core Principles & Ground Rules

1. **Algorithm-First:** Understand mathematical invariants, asymptotic complexity, and edge cases before submitting code.
2. **Correctness Before Complexity:** Write deterministic, verifiable code with zero unhandled edge conditions.
3. **Bounded & Predictable:** Never introduce unbounded queues, maps, history buffers, or thread pools.
4. **Evidence Over Claims:** Back all performance and correctness claims with hand-calculated unit tests, property checks, or JMH benchmarks.
5. **No Flaky Tests:** Concurrency tests must utilize explicit synchronization primitives (`CountDownLatch`, `CompletableFuture`), never arbitrary `Thread.sleep()`.

---

## 🛠️ Development Workflow

### 1. Prerequisites
- **Java Development Kit (JDK):** 21 or higher
- **Apache Maven:** 3.9+ (or use `./mvnw`)

### 2. Branching & Parallel Execution
- `main`: Protected, production-ready branch. Never commit directly to `main`.
- `virus`: Primary lead architecture and API branch (Developer 1).
- `zebra`: Collaborative runtime and infrastructure branch (Developer 2).
- Feature branches: Branch off `virus` or `zebra` depending on ownership.

### 3. Build & Test Commands
```bash
# Clean, compile, and run all JUnit 5 tests
mvn clean test

# Run full verification including Javadoc validation
mvn clean verify
```

---

## 📂 Code Organization Standards

- `com.unicore.api.*`: Clean, fluent, immutable public interfaces and builders.
- `com.unicore.core.*`: Request/Result models, lifecycle state machines, and core exceptions.
- `com.unicore.orchestration.*`: Request validation, strategy resolving, and planning.
- `com.unicore.algorithms.*`: Pure algorithm mechanics isolated from execution runtime.
- `com.unicore.execution.*`: Thread pools, bounded queues, and worker dispatchers.
- `com.unicore.runtime.*`: Complete operational runtime services.
- `com.unicore.metrics.*`: Performance metrics counters, timers, and snapshots.
- `com.unicore.diagnostics.*`: Decision traces and health checks.
- `com.unicore.spi.*`: Extension points for custom algorithms and strategy providers.

---

## 📋 Pull Request Checklist

Before submitting a PR, ensure:
- [ ] Code compiles cleanly under Java 21 with `-Xlint:all`.
- [ ] Comprehensive unit tests cover nominal behavior, boundary limits, and error paths.
- [ ] All queues, caches, and buffers are strictly bounded.
- [ ] Javadoc is written for all public APIs and SPI interfaces.
- [ ] Branch merges cleanly without breaking parallel sprint contracts.
