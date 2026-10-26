# UnirCore — Pre-Study Concept Index

**Folder:** `Study_Material/`  
**Purpose:** Learn the operating-system, algorithm, database, and system-design concepts relevant to UnirCore before implementation.  
**Planned study files:** **60** (this index is separate).

> This is a concept-and-algorithm syllabus, not Java fundamentals or a DSA course. Do not add Java basics, collections tutorials, or generic framework lessons. Each file should explain one OS/system concept or algorithm, its steps/state model, assumptions, complexity where meaningful, trade-offs, failure cases, and how it may apply to UnirCore. A concept being studied does not automatically mean it must be implemented.

## Stage 1 — Operating-system foundations

| No. | Filename | Concept to learn |
|---:|---|---|
| 01 | `01-processes-and-threads.md` | Process versus thread, execution context, isolation, and shared resources. |
| 02 | `02-user-mode-and-kernel-mode.md` | User/kernel modes, system calls, and what a user-space library can and cannot control. |
| 03 | `03-cpu-scheduling-fundamentals.md` | Scheduling goals, workload assumptions, preemption, and scheduling metrics. |
| 04 | `04-first-come-first-served-scheduling.md` | FCFS scheduling algorithm and convoy effect. |
| 05 | `05-shortest-job-first-scheduling.md` | SJF algorithm, burst-time estimates, and starvation trade-offs. |
| 06 | `06-shortest-remaining-time-first.md` | SRTF preemptive scheduling and remaining-time estimates. |
| 07 | `07-priority-scheduling-and-aging.md` | Priority scheduling, starvation, aging, and tie-breaking. |
| 08 | `08-round-robin-scheduling.md` | Round Robin, time quantum, fairness, and quantum trade-offs. |
| 09 | `09-multilevel-queue-scheduling.md` | Multilevel queue scheduling and fixed class assignment. |
| 10 | `10-multilevel-feedback-queue.md` | MLFQ, dynamic priorities, feedback, and parameter trade-offs. |
| 11 | `11-context-switching-and-dispatch-latency.md` | Context switches, dispatch overhead, and model versus real OS costs. |
| 12 | `12-process-synchronization-and-critical-sections.md` | Critical sections, mutual exclusion, progress, and bounded waiting. |
| 13 | `13-mutexes-semaphores-and-monitors.md` | Mutex, binary/counting semaphores, monitors, and use-case differences. |
| 14 | `14-producer-consumer-problem.md` | Bounded-buffer producer–consumer problem and semaphore/monitor solutions. |
| 15 | `15-readers-writers-problem.md` | Readers–writers variants, fairness, and starvation. |
| 16 | `16-dining-philosophers-problem.md` | Dining Philosophers, deadlock conditions, and prevention/avoidance strategies. |
| 17 | `17-deadlock-conditions-and-resource-allocation.md` | Coffman conditions, resource allocation graphs, and deadlock characterization. |
| 18 | `18-deadlock-prevention-avoidance-detection.md` | Prevention, avoidance, detection, recovery, and when each approach fits. |
| 19 | `19-bankers-algorithm.md` | Banker's algorithm, safe states/sequences, and resource-request checks. |
| 20 | `20-starvation-fairness-and-priority-inversion.md` | Starvation, fairness, priority inversion, and mitigation patterns. |
| 21 | `21-virtual-memory-and-paging.md` | Virtual memory, pages, page tables, page faults, and address-space isolation. |
| 22 | `22-page-replacement-algorithms.md` | FIFO, LRU, Optimal, and Clock page replacement; page-fault comparison. |
| 23 | `23-disk-scheduling-algorithms.md` | FCFS, SSTF, SCAN, C-SCAN, LOOK, and C-LOOK disk scheduling. |
| 24 | `24-interrupts-and-event-driven-execution.md` | Interrupts, events, polling, and event-driven execution models. |

## Stage 2 — Core algorithmic policies for systems

| No. | Filename | Concept to learn |
|---:|---|---|
| 25 | `25-queueing-theory-and-little-law.md` | Arrival/service rates, queue length, waiting time, saturation, and Little's Law. |
| 26 | `26-backpressure-and-admission-control.md` | Bounded queues, overload policies, rejection, and backpressure. |
| 27 | `27-token-bucket-rate-limiting.md` | Token bucket refill, burst capacity, permit decisions, and edge cases. |
| 28 | `28-leaky-bucket-rate-limiting.md` | Leaky bucket shaping, queueing behavior, and comparison with token bucket. |
| 29 | `29-fixed-and-sliding-window-rate-limiting.md` | Fixed window, sliding log/window, boundary bursts, and state costs. |
| 30 | `30-round-robin-and-weighted-round-robin.md` | Round Robin and weighted selection for distributing work across targets. |
| 31 | `31-least-connections-and-load-aware-routing.md` | Least-connections selection, load signals, stale measurements, and limitations. |
| 32 | `32-lru-and-fifo-cache-eviction.md` | LRU/FIFO cache eviction, recency/order tracking, and trade-offs. |
| 33 | `33-lfu-and-clock-cache-eviction.md` | LFU and Clock eviction policies; frequency tracking and approximation. |
| 34 | `34-ttl-expiration-and-expiry-cleanup.md` | TTL semantics, lazy versus scheduled cleanup, and time correctness. |
| 35 | `35-retry-backoff-and-jitter.md` | Bounded retry, exponential backoff, jitter, retry budgets, and idempotency. |
| 36 | `36-circuit-breaker-state-machine.md` | Closed/open/half-open states, thresholds, transitions, and recovery. |
| 37 | `37-timeouts-deadlines-and-cancellation.md` | Deadline propagation, timeout budgets, cooperative cancellation, and limits. |
| 38 | `38-resource-pools-and-bulkheads.md` | Resource pooling, isolation/bulkheads, pool sizing, and exhaustion handling. |

## Stage 3 — Distributed systems and system-design foundations

| No. | Filename | Concept to learn |
|---:|---|---|
| 39 | `39-scalability-and-performance-bottlenecks.md` | Vertical/horizontal scaling, bottlenecks, throughput, latency, and saturation. |
| 40 | `40-latency-throughput-and-tail-latency.md` | Percentiles, p95/p99, queueing delay, and why averages can mislead. |
| 41 | `41-load-balancing-architectures.md` | Client-side/server-side load balancing, health checks, failover, and trade-offs. |
| 42 | `42-caching-strategies-and-cache-consistency.md` | Cache-aside, read-through, write-through, write-back, invalidation, staleness. |
| 43 | `43-replication-and-consistency-models.md` | Replication, strong/eventual consistency, read-your-writes, and trade-offs. |
| 44 | `44-cap-theorem-and-partition-tolerance.md` | Network partitions and the CAP trade-off, with limits of simplistic interpretations. |
| 45 | `45-consensus-and-leader-election-overview.md` | Consensus purpose, leader election, quorum, and why distributed coordination is hard. |
| 46 | `46-idempotency-and-duplicate-request-handling.md` | Idempotent operations, deduplication keys, retries, and duplicate side effects. |
| 47 | `47-message-queues-and-delivery-semantics.md` | Queue-based decoupling, at-most/at-least/exactly-once claims, acknowledgments. |
| 48 | `48-event-driven-architecture-and-pub-sub.md` | Events, pub/sub, consumer groups, ordering, and loose coupling. |
| 49 | `49-circuit-breakers-bulkheads-and-fail-fast.md` | Resilience patterns at system boundaries and their interactions. |
| 50 | `50-rate-limiting-in-distributed-systems.md` | Per-process versus global limits, coordination costs, and approximate approaches. |

## Stage 4 — DBMS and data-management concepts

| No. | Filename | Concept to learn |
|---:|---|---|
| 51 | `51-transactions-acid-and-isolation.md` | ACID, transaction boundaries, isolation levels, and anomalies. |
| 52 | `52-locking-mvcc-and-concurrency-control.md` | Shared/exclusive locks, optimistic control, MVCC, conflicts, and deadlocks. |
| 53 | `53-indexes-b-trees-and-hash-indexes.md` | Index purpose, B-tree/hash indexes, read/write costs, and selectivity. |
| 54 | `54-database-connection-pooling.md` | Connection pools, pool sizing, contention, timeouts, and leak prevention. |
| 55 | `55-database-caching-and-invalidation.md` | Query/result caching, stale data, invalidation, and consistency risks. |
| 56 | `56-database-locking-and-deadlock-handling.md` | Lock ordering, deadlock detection, retrying transactions, and safe recovery. |

## Stage 5 — Reliability, observability, and validation

| No. | Filename | Concept to learn |
|---:|---|---|
| 57 | `57-failure-modes-and-fault-isolation.md` | Failure domains, cascading failures, isolation, and graceful degradation. |
| 58 | `58-metrics-logs-traces-and-diagnostics.md` | Counters, latency histograms, logs, traces, cardinality, and bounded diagnostics. |
| 59 | `59-correctness-testing-and-concurrency-testing.md` | Invariants, boundary tests, race tests, controlled time, and reproducibility. |
| 60 | `60-benchmarking-and-performance-validation.md` | Baselines, warm-up, throughput/latency measurement, workload realism, and claims. |

## How to prepare each study file

Use this structure for every file:

1. **Concept:** concise definition and purpose.
2. **Mechanism/algorithm:** numbered steps or state transitions.
3. **Example:** a small worked example, trace, or scenario.
4. **Trade-offs:** benefits, costs, assumptions, and failure cases.
5. **Comparison:** alternatives and when they are preferable.
6. **UnirCore relevance:** what could apply, what is only a simulation, and what is out of scope.
7. **Checkpoint:** 3–5 questions or one small problem to solve without looking.

## Scope guardrails

- This is **not** a Java syntax, Java Collections Framework, or generic DSA syllabus.
- Study operating-system algorithms as algorithms/models. A Java library cannot replace the kernel CPU scheduler, physical memory manager, or disk scheduler.
- Distributed-system topics provide design literacy; they do **not** imply UnirCore v1 will become a distributed service.
- DBMS topics matter for understanding transactions, locking, pooling, and cache consistency; they do **not** imply that UnirCore needs its own database.
- Implementation decisions still require review against the project's API, architecture, constraints, and validation documents.

**Planning decision:** Use 60 focused pre-study files as the baseline. Add or split topics only when an architecture review identifies a genuine gap.
