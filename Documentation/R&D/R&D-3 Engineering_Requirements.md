# UniCore — Technology & Engineering Requirements (Streamlined)

## 1. Project Purpose & Scope

UniCore is a clean, modern Java library for **Operating System algorithms and resource allocation simulations**. 

By eliminating the 40% non-OS and complex distributed layers (rate limiting, circuit breakers, distributed consensus, cluster load balancing), the engineering requirements are focused entirely on core correctness, performance, and clear outcomes.

---

## 2. Refined Technology Stack

| Component | Choice | Purpose |
|---|---|---|
| **Language** | Java 21 LTS | Pattern matching, records, immutable collections, virtual threads |
| **Build System** | Apache Maven 3.9+ | Standard dependency resolution, compilation, and packaging |
| **Testing** | JUnit 5 & AssertJ | Deterministic contract and unit tests |
| **Benchmarking** | OpenJDK JMH | Latency and throughput microbenchmarks |
| **Concurrency** | `java.util.concurrent` | Atomics, Semaphores, ReentrantLocks, Condition variables |
| **Distribution** | Maven Central | Artifact: `com.unicore:unicore:1.0.0-SNAPSHOT` |
| **CI/CD** | GitHub Actions | Automated build, test, and Javadoc verification |

---

## 3. Engineering Quality Gates

1. **Pure Algorithmic Independence:** Algorithms under `com.unicore.algorithms.*` must be pure calculation engines with zero external I/O or network dependencies.
2. **Deterministic Reproducibility:** Every algorithm simulation given the same inputs must produce identical scheduling timelines, page fault numbers, and seek movements.
3. **Clean Error Handling:** Invalid inputs (e.g., negative burst times, zero frames, non-existent cylinder tracks) must throw descriptive `IllegalArgumentException` or domain-specific checked validation errors.
4. **No Third-Party Runtime Dependencies:** Core artifact operates solely on the standard Java runtime library (JDK 21+).
