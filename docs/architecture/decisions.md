# UniCore Architecture Decision Records (ADR)

This document records the foundational architectural decisions made for the UniCore platform.

---

## ADR-001: Java 21 LTS Baseline

- **Status:** Accepted
- **Context:** UniCore requires modern language features, strong memory model semantics, records, pattern matching, and performance improvements.
- **Decision:** Target Java 21 LTS as the minimum supported compiler and runtime version.
- **Consequences:** Eliminates legacy boilerplate and enables high-performance primitives without external dependencies.

---

## ADR-002: Monolithic Maven Artifact with Modular Package Boundaries

- **Status:** Accepted
- **Context:** Multi-module Maven projects introduce dependency management overhead and publishing complexity during early phases.
- **Decision:** Publish a single core Maven artifact (`com.unicore:unicore`) with package-level modularity (`api`, `core`, `algorithms`, `execution`, `runtime`, `spi`).
- **Consequences:** Simplifies consumption for downstream users while maintaining strict logical boundaries inside the JVM.

---

## ADR-003: Strict Bounded Resource Policy

- **Status:** Accepted
- **Context:** Unbounded collections in long-running services lead to memory exhaustion and unpredictable GC pauses.
- **Decision:** Every queue, cache, task history buffer, and retry loop must have an explicit, configurable upper bound.
- **Consequences:** Eliminates silent out-of-memory vulnerabilities. Rejection policies must be explicitly handled.

---

## ADR-004: Pure Algorithm Isolation from Runtime

- **Status:** Accepted
- **Context:** Coupling algorithm math with thread management or network I/O reduces testability and reusability.
- **Decision:** Algorithm implementations under `com.unicore.algorithms.*` must remain pure and free from direct execution or threading side effects.
- **Consequences:** Algorithms can be simulated deterministically in test harnesses and embedded into different runtime engines.
