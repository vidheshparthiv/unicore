# UnirCore --- Existing Solutions, Competitive Analysis, and Reuse Strategy

**Status:** Research-informed proposal. Verify current versions, APIs,
and licenses before implementation.\
**Purpose:** Identify mature existing solutions, what UnirCore should
reuse or learn from them, their trade-offs, and where UnirCore may
create a distinct value proposition.

## 1. Executive position

UnirCore should not claim to have invented scheduling, caching, rate
limiting, retries, circuit breakers, load balancing, or thread pools.
These are established areas with mature implementations. The defensible
product opportunity is a **coherent, explainable, configurable Java API
across selected algorithm families**, with consistent validation,
bounded-resource behavior, strategy explanations, predictable contracts,
and a deliberately small core dependency surface.

For every feature, choose among: (1) use a JDK primitive directly; (2)
wrap a JDK primitive to add lifecycle/contracts; (3) implement domain
logic; (4) integrate a specialist library optionally; or (5) defer the
feature.

The "demerits" below are mainly trade-offs relative to UnirCore's
proposed goals, not universal defects. Do not claim a competitor is
slow, bad, or inflexible without a reproducible version-specific
comparison.

## 2. Existing solutions

### 2.1 JDK concurrency primitives

**What exists:** `ExecutorService`, `ThreadPoolExecutor`,
`BlockingQueue`, `Future`, `Semaphore`, atomics, concurrent collections,
locks, `PriorityQueue`, `Duration`, `Instant`, and `System.nanoTime()`.

**Reuse:** worker threads, queueing, cancellation signals, rejection
handlers, synchronization primitives, common data structures, monotonic
time, and atomic counters.

**Trade-offs for UnirCore's purpose:** JDK primitives are
general-purpose mechanisms, not a complete domain API for scheduling
simulations, strategy explanations, cache policies, retry policy,
circuit-breaker state, or node selection. Thread-pool behavior depends
on queue bounds, thread bounds, and rejection policy. A Java thread pool
is not an OS scheduler and cannot safely force-preempt arbitrary Java
code.

**Decision:** reuse JDK primitives; write only UnirCore-specific
policies, validation, typed outcomes, bounded tracking, and lifecycle
semantics. Oracle's `ThreadPoolExecutor` documentation explains how
queue and pool bounds affect task handling and rejection.
[Source](https://docs.oracle.com/en/java/javase/27/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html).

### 2.2 Caffeine --- Java caching

**What exists:** a mature Java cache with sophisticated eviction,
expiration, and cache-management capabilities.
[Project](https://github.com/ben-manes/caffeine)

**Learn/reuse:** clear cache configuration, bounded capacity, expiration
semantics, and statistics as design references. Consider an optional
adapter for consumers needing advanced caching.

**Trade-offs:** recreating advanced eviction behavior is costly and
risks lower performance or correctness than a mature specialist. A
simple UnirCore LRU/FIFO cache must not claim to match Caffeine without
benchmarks. Making Caffeine a core dependency would burden consumers who
only need scheduling or resilience. Supporting two cache implementations
can confuse users unless use cases are distinct.

**Decision:** implement a small bounded LRU/FIFO/optional-TTL cache only
if owning cache policy is a real product goal. For demanding production
caching, recommend Caffeine or make integration optional rather than
claiming to replace it.

### 2.3 Resilience4j --- retry, circuit breaker, rate limiter, bulkhead, timeout

**What exists:** Resilience4j provides modular fault-tolerance
decorators, including circuit breaker, rate limiter, retry, bulkhead,
and time limiter. Consumers can choose modules and compose decorators;
framework/reactive integrations are separate. Its documentation notes
Java 17 for Resilience4j 2.
[Source](https://resilience4j.readme.io/docs/getting-started)

**Learn/reuse:** modular feature selection, explicit policy
configuration, composable decorators, state visibility, and optional
integrations.

**Trade-offs:** Resilience4j is already mature in resilience-specific
features, so duplicating it merely to increase UnirCore's feature count
is poor strategy. Retry/circuit-breaker composition order affects
attempts and metrics; this is an important design decision for any
library. Consumers may need to learn its APIs or add specific modules.

**Decision:** implement bounded retry/circuit breaker only if their
contracts are clearly scoped and independently tested. Prefer
interoperability or optional adapters for advanced use cases; do not
claim feature parity without evidence.

### 2.4 Bucket4j --- token-bucket rate limiting

**What exists:** a Java rate-limiting library based on token bucket. Its
project documentation describes integer arithmetic, concurrency options,
listeners/diagnostics, configuration changes, and distributed backends.
[Source](https://github.com/bucket4j/bucket4j)

**Learn/reuse:** integer accounting where useful, explicit
capacity/refill configuration, observable permit decisions, and
separation of local from distributed limiting.

**Trade-offs:** Bucket4j already provides specialist and distributed
capabilities. Distributed quotas introduce backend consistency, latency,
and failure behavior beyond an in-process limiter. A smaller UnirCore
API may be easier for cross-family use, but that is a hypothesis to
test---not an established superiority.

**Decision:** use token bucket as a candidate first algorithm only if
UnirCore intends to own and validate it. For production distributed
limiting, prefer Bucket4j or another specialist through an optional
integration/recommendation. Do not reimplement distributed backends in
the first release.

### 2.5 Apache Commons Pool --- object pooling

**What exists:** APIs and implementations for object pooling, lifecycle
management, tracking, and monitoring.
[Source](https://commons.apache.org/proper/commons-pool/)

**Learn/reuse:** explicit object lifecycle, validation hooks, capacity,
acquisition/release, and monitoring semantics.

**Trade-offs:** a generic object pool is not a universal OS resource
manager. A custom lease abstraction adds value only for a specific
UnirCore contract. Pooling requires correct release/ownership semantics;
mistakes can leak or corrupt resources.

**Decision:** use `Semaphore` or bounded queues for simple permits.
Evaluate Commons Pool if real object pooling becomes a requirement
rather than casually building a general-purpose pool.

### 2.6 Scheduling/OS-simulation implementations

Many academic examples implement FCFS, SJF, SRTF, priority, and Round
Robin to teach waiting/turnaround calculations.

**Learn/reuse:** standard definitions, known edge cases, independent
reference cases, and metrics such as waiting and turnaround time.

**Trade-offs:** classroom examples may assume all jobs arrive at time
zero or omit tie-breaking, thread safety, bounded resources, lifecycle,
typed failures, extension points, and API stability. A live executor
cannot safely emulate OS preemption simply by assigning a time quantum.

**Decision:** implement a clearly bounded scheduling simulator if it
serves the product. State assumptions; do not present it as replacing
Linux/Windows/macOS scheduling.

## 3. Feature-by-feature decision

  --------------------------------------------------------------------------------------------
  Feature           Existing baseline          UnirCore approach         Initial dependency
                                                                         decision
  ----------------- -------------------------- ------------------------- ---------------------
  Task execution    JDK                        Thin                      JDK only
                    executors/futures/queues   bounded/lifecycle-aware   
                                               adapter                   

  Scheduling        Textbook                   Selected rules with       Custom domain logic +
  simulation        algorithms/reference       explicit assumptions and  JDK collections
                    models                     metrics                   

  Cache             Caffeine and other mature  Small bounded policy API  No mandatory cache
                    caches                     only if justified         dependency

  Rate limiting     Bucket4j, Resilience4j     One local policy with     No mandatory
                                               typed decisions if owned  dependency; optional
                                                                         adapter later

  Retry             Resilience4j and others    Bounded policy engine if  Validate against
                                               coherent API justifies it specialist
                                                                         alternatives

  Circuit breaker   Resilience4j               Small explicit state      Optional integration
                                               machine if included       may be better

  Load balancing    Framework/application      In-process node selection JDK
                    libraries                  only                      collections/atomics

  Resource pool     Commons Pool, JDK          Semaphore for permits;    No general pool
                    `Semaphore`                Commons Pool for object   initially
                                               pooling if needed         

  Metrics           JDK counters; Micrometer   Bounded snapshot          JDK-only core;
                    ecosystem                  interface                 optional exporter

  Tests             JUnit ecosystem            Contract, boundary,       Test-scope only
                                               concurrency tests         

  Benchmarks        JMH                        Reproducible workload     Benchmark-only
                                               benchmarks                

  Framework         Spring and others          Optional adapters         Separate optional
  integration                                                            artifact
  --------------------------------------------------------------------------------------------

## 4. What UnirCore replicates versus reuses

"Replicate" means implementing known algorithm behavior, not copying
another project's source.

**Implement as domain behavior, if approved:**
FCFS/SJF/SRTF/priority/Round Robin; LRU/FIFO/optional TTL; token-bucket
accounting; bounded retry/backoff; circuit-breaker state machine; Round
Robin/Weighted Round Robin; compatibility filtering and explainable
selection.

**Reuse instead of recreating:** thread pools, futures, queues, locks,
concurrent maps, atomics, time types, UUIDs, JUnit, and JMH. For
advanced cache/rate-limiter/resilience/object-pool requirements, prefer
specialist libraries or optional adapters.

**Do not copy:** source code without license review; competitor
claims/defaults without evidence; internals of mature libraries merely
to imitate performance; distributed coordination without a
consistency/failure model.

## 5. Candidate innovations

These are potential differentiators to validate, not claims of invented
algorithms.

1.  **Cross-family consistency:** shared validation, typed outcomes,
    timeouts, lifecycle, and bounded state.
2.  **Explainable strategy selection:** show compatible candidates,
    rejected candidates, and documented selection rule.
3.  **Simulation/execution separation:** prevent a scheduling model from
    pretending to preempt arbitrary Java code.
4.  **Bounded-by-default design:** explicit bounds for queues, task
    history, cache, retries, metrics dimensions, and diagnostics.
5.  **Dependency-conscious core:** avoid mandatory frameworks and make
    integrations optional.
6.  **Contract-level quality:** reusable contract tests, deterministic
    time tests, concurrency tests, and compatibility checks.

These are established engineering practices applied consistently, not
inventions of the underlying algorithms.

## 6. Competitive risks and honest limitations

-   Initial implementations may be less feature-rich or less optimized
    than specialist libraries.
-   A broad catalogue increases maintenance and testing burden.
-   A unified API can become awkward if unlike domains are forced into
    one abstraction.
-   Automatic selection may be less suitable than explicit selection for
    expert consumers.
-   Custom cache/limiter/breaker code can contain subtle concurrency
    defects that mature alternatives have spent years addressing.
-   Convenience is not a reason to replace a specialist tool that better
    fits a production use case.

## 7. Evidence required before superiority claims

1.  Select exact versions/configurations of comparison libraries.
2.  Define equivalent semantics and workload sizes.
3.  Run JMH in a documented environment.
4.  Compare latency/throughput, allocation, memory growth, correctness,
    and concurrency.
5.  Publish conditions and limitations, not just a percentage.
6.  Test integration usability: time to integrate, configuration steps,
    clarity of errors, ease of replacing strategies.
7.  Remove or narrow claims unsupported by results.

## 8. Sources

-   Oracle Java `ThreadPoolExecutor`:
    https://docs.oracle.com/en/java/javase/27/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html
-   Resilience4j Getting Started:
    https://resilience4j.readme.io/docs/getting-started
-   Resilience4j RateLimiter:
    https://resilience4j.readme.io/docs/ratelimiter
-   Bucket4j: https://github.com/bucket4j/bucket4j
-   Apache Commons Pool: https://commons.apache.org/proper/commons-pool/
-   Caffeine: https://github.com/ben-manes/caffeine

Verify current versions, API details, supported JDKs, and licenses
before making implementation claims.

## Final recommendation

Build UnirCore around **stable contracts, explainable strategy
selection, safe defaults, and coherent consumer experience**. Reuse JDK
mechanisms and specialist libraries where they are the better
engineering choice. Implement classic algorithms only for defined use
cases, and prove any performance or usability advantage with
reproducible evidence.
