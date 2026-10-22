# UnirCore --- Deep Technical Function-Level Architecture Plan

**Status:** Proposed pre-implementation specification. This is a design
plan, not existing source code.\
**Purpose:** Fix the planned file structure, function responsibilities,
algorithms, call graph, dependency direction, and the boundary between
JDK reuse and custom implementation.\
**Scope:** Technical implementation only. Deployment and general project
promotion are excluded.

## 1. Core architecture decisions

UnirCore is a reusable Java library that runs inside the consumer
application's JVM. It is not a hosted service and does not replace
OS-kernel functions. The first release should be one Maven artifact with
package-level modularity. Split into multiple artifacts only when an
actual dependency-isolation need is demonstrated.

Architectural rules:

1.  Public APIs define consumer-visible contracts; runtime classes
    implement them.
2.  Validation precedes algorithm execution.
3.  Workload normalization is separate from strategy selection.
4.  Strategy selection is separate from algorithm mechanics.
5.  Algorithms never call runtime service implementations or
    orchestration.
6.  Metrics observe outcomes; they never decide algorithm correctness.
7.  Framework integrations depend inward on public API/SPI; core never
    imports framework types.
8.  Reuse JDK mechanisms for general-purpose concurrency/data
    structures; implement UnirCore-specific policies.
9.  Timeouts use `Duration`; elapsed-time calculations use monotonic
    time (`System.nanoTime()` via an internal abstraction); wall-clock
    timestamps use `Instant`.
10. Queues, cache capacity, retry attempts, task history, metrics
    dimensions, and diagnostic retention must be bounded.
11. Automatic selection is deterministic and explainable, not a claim of
    globally optimal results.
12. Every public method needs a documented contract, error behavior,
    concurrency guarantee, and test.

## 2. Repository layout

``` text
unircore/
├── pom.xml
├── README.md
├── LICENSE
├── NOTICE
├── CHANGELOG.md
├── CONTRIBUTING.md
├── .github/workflows/{ci.yml,publish-central.yml}
├── src/main/java/com/unircore/
│   ├── api/
│   ├── core/
│   ├── workload/
│   ├── strategy/
│   ├── orchestration/
│   ├── algorithms/
│   │   ├── scheduling/
│   │   ├── cache/
│   │   ├── ratelimit/
│   │   ├── resilience/
│   │   └── loadbalance/
│   ├── execution/
│   ├── runtime/
│   ├── metrics/
│   ├── diagnostics/
│   └── spi/
├── src/test/java/com/unircore/{unit,contract,integration,concurrency,testkit}/
├── benchmarks/src/main/java/com/unircore/benchmarks/
├── examples/
└── docs/{architecture,algorithms,api}/
```

The target inventory below contains **96 production Java files plus
support/build/test/benchmark/example files**. The numbered list expands
some concepts into separate files for clarity. Do not create empty
placeholders just to meet a file count: combine files when they have no
independent responsibility, and defer unapproved features.

## 3. Runtime request paths

### 3.1 Scheduling

`Scheduler.schedule(request)` → validate request → normalize jobs →
analyze workload → resolve compatible strategy → run algorithm →
validate output → calculate schedule metrics → record metrics → return
typed result.

### 3.2 Task execution

`ExecutionService.submit(request)` → validate → assign ID if needed →
wrap task metadata → submit to bounded JDK executor → update task
state/history → record metrics → return accepted/rejected result.

### 3.3 Cache

`Cache.get(key)` → look up entry → check TTL if enabled → update
eviction-policy state on hit → update bounded statistics → return
hit/miss.\
`Cache.put(key,value)` → validate → replace/insert → enforce capacity
via policy → update expiry/statistics.

### 3.4 Rate limiting

`RateLimiter.tryAcquire(n)` → validate permits → read monotonic time →
refill token bucket → grant/deny → record metric. A waiting API
additionally uses a deadline and an interruption-aware wait mechanism.

### 3.5 Retry

`RetryExecutor.execute(operation, policy)` → create deadline → run
attempt → classify failure → check attempt cap/cancellation/deadline →
calculate bounded backoff → wait → retry or propagate final cause.

### 3.6 Circuit breaker

`executeProtected(operation)` → breaker state check → reject if open or
admit call → execute callback → record success/failure → transition
state if thresholds require → return/propagate outcome.

### 3.7 Load balancing

`select(request)` → read coherent node snapshot → validate/filter
eligible nodes → apply Round Robin or Weighted Round Robin → return
selected node or explicit no-eligible-node outcome.

Not every service needs every stage. Do not force a cache lookup through
workload analysis or strategy resolution.

------------------------------------------------------------------------

# 4. File-by-file inventory

The file IDs (F01, F02, etc.) are used in the call graph and
relationship tables. Method names are planned names; signatures may be
refined before coding but must remain consistent across API, JavaDoc,
and tests.

## A. Repository/build/docs

### F01 --- `pom.xml`

**Owns:** Maven coordinates `com.unircore:unircore`, version, Java
baseline, compiler/test plugins, source/Javadoc artifacts, dependency
scopes.\
**Configures:** JUnit in test scope; JMH only in benchmark scope; no
mandatory framework/cloud/database dependencies.\
**Relations:** F02/F03 invoke Maven goals; consumer examples compile
against the packaged JAR.

### F02 --- `.github/workflows/ci.yml`

**Owns:** clean build, test execution, package validation, and quality
gates on changes.\
**Calls:** Maven lifecycle from F01. Does not publish ordinary pull
requests.

### F03 --- `.github/workflows/publish-central.yml`

**Owns:** tag-triggered release verification and Maven Central
publication.\
**Calls:** checks tag/POM version agreement, builds source/Javadoc
artifacts, uses configured signing/publishing secrets.

### F04 --- `README.md`

**Owns:** concise installation, supported Java baseline, small real
examples, maturity labels, links to detailed docs. Examples must use
actual public API.

### F05 --- `LICENSE`

**Owns:** project license terms.

### F06 --- `NOTICE`

**Owns:** third-party notices when legally required.

### F07 --- `CHANGELOG.md`

**Owns:** released behavior changes, deprecations, and compatibility
notes.

### F08 --- `CONTRIBUTING.md`

**Owns:** contribution, test, review, and release expectations.

### F09 --- `docs/architecture/decisions.md`

**Owns:** architecture decisions and rationale, including dependency
choices and deferred features.

### F10 --- `docs/algorithms/algorithm-catalog.md`

**Owns:** algorithm purpose, constraints, complexity, use cases, and
implementation status.

### F11 --- `docs/api/consumer-guide.md`

**Owns:** consumer-oriented service selection and configuration
guidance.

## B. Public API

### F12 --- `api/UnirCore.java`

**Owns:** optional convenience factory for service builders. Planned
methods: `scheduler()`, `executionService()`, `cacheBuilder()`,
`rateLimiterBuilder()`, `retryExecutorBuilder()`,
`circuitBreakerBuilder()`, `loadBalancerBuilder()`.\
**Calls:** F94 factory/builders only. No algorithm logic. Keep only if
it simplifies usage.

### F13 --- `api/Scheduler.java`

**Methods:** `schedule(SchedulingRequest)`,
`selectStrategy(WorkloadProfile)`, `estimate(SchedulingRequest)`,
`explain(SchedulingRequest)`, `validate(SchedulingRequest)`.\
**Implemented by:** F83. `validate` must not execute a schedule;
`estimate` must not imply measured runtime.

### F14 --- `api/ExecutionService.java`

**Methods:** `submit(TaskSubmissionRequest)`,
`submitAll(List<TaskSubmissionRequest>)`, `cancel(TaskId)`,
`inspect(TaskId)`, `shutdown(ShutdownMode, Duration)`, `close()`.\
**Implemented by:** F84. Batch partial/all-or-none behavior must be
specified.

### F15 --- `api/Cache.java`

**Methods:** `get(K)`, `put(K,V)`, `putIfAbsent(K,V)`, `invalidate(K)`,
`invalidateAll()`, `stats()`, optionally `close()`.\
**Implemented by:** F85. Specify null policy, TTL, replacement,
capacity, and thread safety.

### F16 --- `api/RateLimiter.java`

**Methods:** `acquire(int)`, `tryAcquire(int)`,
`tryAcquire(int, Duration)`, `state()`.\
**Implemented by:** F86. Distinguish immediate denial from timed
waiting.

### F17 --- `api/RetryExecutor.java`

**Methods:** `execute(Callable<T>, RetryPolicy)`, optionally
`execute(Callable<T>)` with configured defaults.\
**Implemented by:** F87. Never assume arbitrary callbacks are
idempotent.

### F18 --- `api/CircuitBreaker.java`

**Methods:** `executeProtected(Callable<T>)`, `state()`, `reset()`,
`config()`.\
**Implemented by:** F88.

### F19 --- `api/LoadBalancer.java`

**Methods:** `select(SelectionRequest)`,
`updateNodes(Collection<Node>)`, `inspect()`.\
**Implemented by:** F89.

### F20 --- `api/ResourceManager.java` (conditional)

**Methods:** `acquire(ResourceType, Duration)`,
`release(ResourceLease)`, `inspect(ResourceType)`.\
**Implemented by:** F90 only if a concrete resource-lease use case is
approved.

### F21 --- `api/UnirCoreException.java`

**Owns:** base exception type for library failures; constructors
preserve message/cause. No algorithm logic.

### F22 --- `api/ValidationException.java`

**Owns:** invalid request/configuration reporting. Used by validators
before algorithm execution.

### F23 --- `api/UnsupportedCapabilityException.java`

**Owns:** reports that no strategy satisfies a mandatory requested
capability. Used by strategy resolution.

### F24 --- `api/OperationTimeoutException.java`

**Owns:** timeout exception where contract specifies an exception rather
than a typed result. Not used for normal rate-limit denial.

### F25 --- `api/SchedulingRequest.java`

**Owns:** immutable job collection, strategy selection mode/ID, and
optional workload hints.\
**Relations:** F83 → F30 validator → F35 normalizer.

### F26 --- `api/SchedulingResult.java`

**Owns:** selected strategy, schedule/timeline, defined metrics, and
result metadata. Constructed only after output validation.

### F27 --- `api/WorkloadProfile.java`

**Owns:** normalized workload features such as job count, burst-estimate
availability, preemption/fairness requirements, and workload kind.
Produced by F37.

### F28 --- `api/StrategyExplanation.java`

**Owns:** chosen strategy, candidate filtering, rejected-candidate
reasons, and rule IDs. Produced by F45/F46.

### F29 --- `api/TaskSubmissionRequest.java`

**Owns:** callback, optional task/correlation ID, and deadline metadata.
Validated by F31.

### F30 --- `api/TaskId.java`

**Owns:** typed task identifier used by submission, cancellation,
inspection, and diagnostics.

### F31 --- `api/TaskSnapshot.java`

**Owns:** immutable task state, timestamps when available, cancellation
state, and sanitized failure summary. Produced from F78 registry.

### F32 --- `api/CacheStats.java`

**Owns:** immutable hit/miss/eviction/expiry/size snapshot. Produced by
F63/F64 and exposed by F85.

### F33 --- `api/RateLimitDecision.java`

**Owns:** granted/denied result and optional retry-after estimate.
Normal denial is not exceptional.

### F34 --- `api/Node.java`

**Owns:** immutable node identity, weight, and explicitly supported
metadata. Node health must not be guessed.

### F35 --- `api/NodeSelection.java`

**Owns:** selected node, strategy ID, and selection metadata.

### F36 --- `api/HealthSnapshot.java`

**Owns:** local service/lifecycle health summary, not proof of external
endpoint reachability.

## C. Core

### F37 --- `core/RequestValidator.java`

**Methods:** `validateSchedulingRequest`, `validateTaskRequest`,
`validateNodeSet`, `validateDuration`, `requirePositive`,
`requireNonNegative`.\
**Calls:** family-specific checks; never selects an algorithm.

### F38 --- `core/TaskValidator.java`

**Methods:** `validate(TaskSubmissionRequest)`,
`validateDeadline(...)`.\
**Called by:** F84 before queue insertion; invalid inputs map to F22.

### F39 --- `core/TimeSource.java`

**Methods:** `nanoTime()`, `instant()`. Production delegates to
`System.nanoTime()`/`Instant.now()`; tests provide a controllable
implementation. Keep internal unless public injection is necessary.

### F40 --- `core/Deadline.java`

**Methods:** `after(Duration, TimeSource)`, `remainingNanos()`,
`isExpired()`, `throwIfExpired()`.\
**Called by:** retry, execution, waiting rate limiter, resource
acquisition.

### F41 --- `core/IdGenerator.java`

**Methods:** `newTaskId()`, `newCorrelationId()`. Use JDK UUID unless a
measured requirement justifies another scheme.

### F42 --- `core/ServiceLifecycle.java`

**Methods:** `ensureOpen()`, `beginClose()`, `isClosed()`,
`awaitTermination(Duration)`.\
**Called by:** runtime services for lifecycle enforcement.

### F43 --- `core/ArgumentChecks.java`

**Methods:** small null/range/positive-duration/non-empty checks. Used
by F37/F38/configuration constructors. Do not grow into a generic
validation framework.

### F44 --- `core/OperationContext.java`

**Owns:** correlation ID, optional deadline, bounded diagnostic context.
Flows from orchestration/runtime to metrics. Must not hold unbounded
user metadata.

### F45 --- `core/FailureClassifier.java`

**Methods:** `isRetryable(Throwable, RetryPolicy)`.\
**Called by:** F87 retry engine. Must use explicit policy; never treat
every exception as transient.

### F46 --- `core/CloseSupport.java` (conditional)

**Owns:** shared cleanup only if try-with-resources/`AutoCloseable` is
insufficient. Prefer standard Java cleanup over custom helpers.

## D. Workload analysis

### F47 --- `workload/JobNormalizer.java`

**Method:** `normalize(SchedulingRequest)`. Converts public jobs into
validated internal representations. Called by F83 after F37.

### F48 --- `workload/NormalizedJob.java`

**Owns:** immutable validated job fields consumed by scheduling
algorithms. Avoid leaking mutable caller objects into algorithms.

### F49 --- `workload/WorkloadAnalyzer.java`

**Methods:** `analyze(List<NormalizedJob>)`, `profile(...)`. Produces
F27 for F44/F45 strategy logic. Does not predict exact runtime without
evidence.

### F50 --- `workload/WorkloadConstraints.java`

**Methods:** `mandatoryCapabilities()`, `preferences()`,
`isSatisfiedBy(Capabilities)`. Hard constraints filter candidates;
preferences rank compatible candidates only.

### F51 --- `workload/JobMetricsCalculator.java`

**Methods:** `waitingTime`, `turnaroundTime`, `responseTime`,
`averages`. Calculates metrics only where the chosen schedule model
defines them; avoids duplicated formulas in algorithms.

## E. Strategy and SPI

### F52 --- `strategy/StrategyId.java`

**Owns:** stable IDs for built-in strategies, used in results, registry,
diagnostics, and benchmarks.

### F53 --- `strategy/Capabilities.java`

**Owns:** typed capability metadata: preemption, required estimates,
weighting, bounded waiting, and supported workload forms.

### F54 --- `strategy/StrategyDescriptor.java`

**Owns:** strategy ID, family, capabilities, configuration requirements,
maturity status.

### F55 --- `strategy/StrategyRegistry.java`

**Methods:** `register`, `find`, `candidates`. Reject duplicate IDs
unless replacement semantics are explicitly designed.

### F56 --- `strategy/StrategyResolver.java`

**Methods:** `resolve(profile, constraints, mode)`, `explain(...)`.\
**Calls:** F50 compatibility checks, F55 candidates, F57 policy, F58
explanation builder. Explicit choice is never silently replaced.

### F57 --- `strategy/DefaultSelectionPolicy.java`

**Methods:** family-specific deterministic rule evaluation/ranking. Used
only when automatic selection was requested and meaningful validated
alternatives exist. No network calls or opaque optimization model.

### F58 --- `strategy/StrategyExplanationBuilder.java`

**Method:** `build(selectionDecision)`. Creates F28 with chosen/rejected
candidates and rule IDs; avoids leaking internal object dumps or
sensitive data.

### F59 --- `spi/StrategyProvider.java`

**Methods:** `descriptor()`, `createStrategy(configuration)` or
family-specific equivalent. Allows consumer extensions without changing
UnirCore internals.

### F60 --- `spi/StrategyConfiguration.java`

**Owns:** validated custom-strategy configuration. Prefer typed fields
over an arbitrary map.

### F61 --- `spi/AlgorithmStrategy.java` (conditional)

**Owns:** common strategy interface only if families share a genuinely
stable contract. Otherwise use family-specific interfaces; avoid casts
and nullable universal parameters.

## F. Scheduling algorithms

### F62 --- `algorithms/scheduling/SchedulingAlgorithm.java`

**Methods:** `strategyId()`,
`schedule(List<NormalizedJob>, SchedulingOptions)`, `capabilities()`.
Implemented by F64--F68 and invoked by F83 after F56 selection.

### F63 --- `algorithms/scheduling/ScheduleValidator.java`

**Method:** `validateSchedule(output, input, options)`. Checks job
coverage, ordering/time invariants, and prohibited negative values
before F26 is built.

### F64 --- `algorithms/scheduling/FcfsScheduler.java`

**Algorithm:** First-Come, First-Served. Uses arrival/ready order with
stable tie-break. Complexity is O(n) if already ordered or O(n log n) if
it sorts. Follows F62 contract.

### F65 --- `algorithms/scheduling/SjfScheduler.java`

**Algorithm:** non-preemptive Shortest Job First. Requires burst
estimates; arrival/simulation semantics must be explicit. Typical sort
is O(n log n); event simulation may cost more.

### F66 --- `algorithms/scheduling/SrtfScheduler.java`

**Algorithm:** Shortest Remaining Time First. Selects the ready job with
least remaining estimated time at defined scheduling events. Requires
explicit preemption/event model; release only after these semantics are
tested.

### F67 --- `algorithms/scheduling/PriorityScheduler.java`

**Algorithm:** priority scheduling. Defines whether higher or lower
number wins and stable tie-break. Aging is a separate policy, not an
implicit behavior.

### F68 --- `algorithms/scheduling/RoundRobinScheduler.java`

**Algorithm:** Round Robin simulation with strictly positive time
quantum. Must not claim to preempt arbitrary Java callables. Uses F69
options and F70 timeline model.

### F69 --- `algorithms/scheduling/SchedulingOptions.java`

**Owns:** time quantum, priority direction, tie-break policy, simulation
assumptions. Irrelevant options should be rejected rather than silently
ignored.

### F70 --- `algorithms/scheduling/ScheduleTimeline.java`

**Owns:** internal normalized events/time slices for simulation
algorithms; used by F63/F51 and converted to public F26 result.

## G. Cache

### F71 --- `algorithms/cache/CachePolicy.java`

**Methods:** `onInsert(key)`, `onAccess(key)`,
`selectEvictionCandidate()`, `onRemove(key)`. Owns eviction policy only,
not storage/API.

### F72 --- `algorithms/cache/BoundedCacheEngine.java`

**Methods:** `get`, `put`, `putIfAbsent`, `invalidate`, `clear`, `size`,
`snapshotStats`, `evictIfRequired`. Owns storage, capacity, replacement,
and consistency. Uses JDK collections and F71 policy.

### F73 --- `algorithms/cache/LruPolicy.java`

**Algorithm:** Least Recently Used. Access updates recency. JDK
`LinkedHashMap` access-order can provide a useful mechanism; custom code
adds capacity/concurrency/expiry semantics.

### F74 --- `algorithms/cache/FifoPolicy.java`

**Algorithm:** First In, First Out. Access does not change insertion
order. JDK map/deque structures may be used.

### F75 --- `algorithms/cache/ExpiryIndex.java` (conditional)

**Methods:** `isExpired(entry, now)`, `removeExpired(...)`. Uses F39
monotonic time. Begin with lazy expiry if it meets requirements; do not
add a background thread without need.

### F76 --- `algorithms/cache/CacheStatistics.java`

**Methods:** record hit/miss/eviction/expiry; `snapshot()`. Produces
public F32 and must define snapshot consistency.

## H. Rate limiting

### F77 --- `algorithms/ratelimit/RateLimitPolicy.java`

**Methods:** `tryAcquire(permits, nowNanos)`, `state(nowNanos)`,
optional `nextAvailableNanos`. Returns decisions; waiting belongs in
runtime.

### F78 --- `algorithms/ratelimit/TokenBucketPolicy.java`

**Algorithm:** token bucket. Tracks capacity, tokens, refill rate, last
monotonic refill time. Refill to capacity, grant only when enough tokens
exist. Normally O(1) excluding contention.

### F79 --- `algorithms/ratelimit/RateLimiterConfig.java`

**Owns:** validated capacity/rate/time settings. Constructed before F78.

### F80 --- `algorithms/ratelimit/RateLimiterState.java`

**Owns:** immutable current permit/capacity/rate snapshot. Does not
expose mutable bucket state.

## I. Resilience

### F81 --- `algorithms/resilience/RetryPolicy.java`

**Owns:** maximum attempts, retry predicate, bounded backoff, optional
jitter, and deadline rules. Initial-attempt counting must be explicit.

### F82 --- `algorithms/resilience/BackoffStrategy.java`

**Method:** `delayForAttempt(int)`. Computes bounded delay and handles
overflow. Test with deterministic time/randomness.

### F83 --- `algorithms/resilience/RetryEngine.java`

**Method:** `execute(Callable<T>, RetryPolicy, Deadline)`. Calls
operation, F45 failure classifier, F82 delay, F39/F40 time/deadline.
Stops on attempt cap, non-retryable failure, cancellation, interruption,
or deadline. Preserves final cause.

### F84 --- `algorithms/resilience/CircuitBreakerConfig.java`

**Owns:** failure thresholds, open duration, and half-open trial limit.
Validated before state machine creation.

### F85 --- `algorithms/resilience/CircuitBreakerStateMachine.java`

**Methods:** `beforeCall`, `recordSuccess`, `recordFailure`, `state`,
`reset`. Owns CLOSED/OPEN/HALF_OPEN transitions and concurrent
admission.

### F86 --- `algorithms/resilience/CircuitBreakerEngine.java`

**Method:** `executeProtected(Callable<T>)`. Checks F85, runs callback
only when allowed, records outcome, distinguishes breaker rejection from
operation failure.

**Composition rule:** explicitly document whether retry is inside or
outside the breaker; the two orders produce different traffic and
metrics.

## J. Load balancing

### F87 --- `algorithms/loadbalance/NodeRegistry.java`

**Methods:** `replaceNodes`, `snapshot`. Publishes a validated immutable
node snapshot so readers never observe partial mutation.

### F88 --- `algorithms/loadbalance/NodeEligibilityPolicy.java`

**Method:** `eligibleNodes(request, nodes)`. Applies explicit
eligibility criteria; must not invent health signals.

### F89 --- `algorithms/loadbalance/NodeSelectionStrategy.java`

**Method:** `select(eligibleNodes)`. Stable internal selection contract.

### F90 --- `algorithms/loadbalance/RoundRobinStrategy.java`

**Algorithm:** Round Robin with thread-safe cursor. Tests cover
concurrent selection and membership changes.

### F91 --- `algorithms/loadbalance/WeightedRoundRobinStrategy.java`

**Algorithm:** weighted distribution using validated positive weights.
Test long-run distribution with tolerance appropriate to the chosen
implementation.

### F92 --- `algorithms/loadbalance/NodeSetValidator.java`

**Method:** `validate(nodes)`. Checks duplicate IDs, invalid weights,
and empty-set policy before F87 updates.

**Deferred:** Least Connections until a reliable source and ownership
model for active-connection counts exists.

## K. Execution/resource management

### F93 --- `execution/BoundedTaskExecutor.java`

**Methods:** `submit`, `shutdown`, `awaitTermination`. Wraps JDK
`ThreadPoolExecutor`, bounded `BlockingQueue`, `Future`, and rejection
handler. Adds UnirCore queue/rejection/lifecycle semantics; does not
reimplement worker threads.

### F94 --- `execution/TaskRegistry.java`

**Methods:** `register`, `transition`, `find`, `removeExpiredHistory`.
Maintains bounded task snapshots; completed history must not grow
forever.

### F95 --- `execution/TaskStateMachine.java`

**Owns:** valid task transitions, e.g. SUBMITTED → QUEUED → RUNNING →
SUCCEEDED/FAILED, with explicit cancellation/rejection states. Prevents
impossible transitions.

### F96 --- `execution/TaskCancellation.java`

**Methods:** `requestCancel`, optional `interruptIfRunning`. Uses JDK
`Future.cancel`/interruption where appropriate. Arbitrary user code may
ignore interruption; never promise forced termination.

### F97 --- `execution/TaskEnvelope.java`

**Owns:** callback plus immutable task metadata/context. Connects API
submission to executor worker and state tracking.

### F98 --- `execution/ResourceLease.java` (conditional)

**Owns:** lease identity, resource type, and release ownership. Release
idempotency must be explicitly specified.

### F99 --- `execution/BoundedResourcePool.java` (conditional)

**Methods:** `acquire(timeout)`, `release(lease)`, `inspect`. Reuse
`Semaphore`/`BlockingQueue` when semantics fit; custom code handles
lease ownership and timeout accounting.

## L. Runtime service implementations

### F100 --- `runtime/DefaultScheduler.java`

**Implements:** F13.\
`schedule()` → F37 validate → F47 normalize → F49 analyze → F56 resolve
→ selected F62 algorithm (F64--F68) → F63 validate output → F51 metrics
→ F26 result → metrics recorder.\
`selectStrategy()` → F56. `explain()` → F56/F58. `validate()` → F37
only. `estimate()` returns documented estimates only.

### F101 --- `runtime/DefaultExecutionService.java`

**Implements:** F14.\
`submit()` → F38 → F41 ID → F97 envelope → F93 executor → F95 state →
F94 registry → metrics.\
`cancel()` → F94 lookup → F96 cancellation → state update.\
`inspect()` → F94 snapshot.\
`shutdown()` → F42 lifecycle → F93 shutdown/wait under deadline.

### F102 --- `runtime/DefaultCache.java`

**Implements:** F15.\
Public call → lifecycle check if needed → F72 engine → F71 policy
(F73/F74) → F75 expiry if enabled → F76 stats → F32 snapshot.

### F103 --- `runtime/DefaultRateLimiter.java`

**Implements:** F16.\
Validate permits/timeout → F78 policy with F39 time → F33 decision →
metrics. Waiting variants use F40 deadline and interruption-aware
waiting; token-bucket arithmetic alone does not implement waiting.

### F104 --- `runtime/DefaultRetryExecutor.java`

**Implements:** F17.\
Validate policy → F40 deadline → F83 retry engine → F45 classifier/F82
delay → metrics → result or preserved final failure.

### F105 --- `runtime/DefaultCircuitBreaker.java`

**Implements:** F18.\
F86 engine → F85 state machine → callback → record outcome → metrics.

### F106 --- `runtime/DefaultLoadBalancer.java`

**Implements:** F19.\
F92 validate node updates → F87 publish snapshot → F88 filter → F89
strategy (F90/F91) → F35 result → metrics.

### F107 --- `runtime/DefaultResourceManager.java` (conditional)

**Implements:** F20.\
Validate resource/timeout → F99 pool → F98 lease → metrics/snapshot.
Include only after confirming a concrete initial use case.

### F108 --- `runtime/UnirCoreFactory.java`

**Owns:** service assembly and dependency injection for F100--F107.\
**Methods:** service-specific builders/factories. Avoid global mutable
singletons. Make ownership of executors, clocks, metrics, and cleanup
explicit.

## M. Metrics and diagnostics

### F109 --- `metrics/MetricsRecorder.java`

**Methods:** `increment`, `recordDuration`, `snapshot`. Narrow
observation interface called by runtime services.

### F110 --- `metrics/InMemoryMetricsRecorder.java`

**Owns:** JDK-only bounded counters/timing aggregates. `LongAdder` may
suit contended counters; use atomics/locks where required. Do not
promise globally atomic snapshots unless implemented.

### F111 --- `metrics/MetricsSnapshot.java`

**Owns:** immutable counters/timing aggregates. No unbounded
per-task/per-request metric labels.

### F112 --- `diagnostics/DiagnosticsService.java`

**Methods:** `health`, optional `explain(correlationId)`. Combines
lifecycle, metrics, and bounded task/event snapshots into F36.

### F113 --- `diagnostics/DiagnosticEvent.java`

**Owns:** sanitized event record for selected state transitions; no
secrets, raw payloads, or arbitrary user data.

### F114 --- `diagnostics/BoundedDiagnosticBuffer.java`

**Methods:** `append`, `snapshot`, `clear`. Bounded retention. Use a JDK
queue if sufficient; custom ring buffer only if a measured requirement
exists.

## N. Test/benchmark/example files

These are not runtime dependencies.

### F115 --- `src/test/.../ControlledTimeSource.java`

**Purpose:** fake implementation of F39 for deterministic TTL, refill,
retry, deadline, and breaker tests.

### F116 --- `src/test/.../RecordingMetricsRecorder.java`

**Purpose:** captures metric events to verify integration behavior.

### F117 --- `src/test/.../TestTasks.java`

**Purpose:** deterministic success, failure, blocking, cancellation, and
interruption callbacks.

### F118 --- `src/test/.../AlgorithmContractAssertions.java`

**Purpose:** reusable assertions for strategy IDs, capabilities, output
invariants, and expected failures.

### F119 --- `benchmarks/.../SchedulingBenchmark.java`

**Purpose:** JMH measurements for scheduling size/selection cost.

### F120 --- `benchmarks/.../CacheBenchmark.java`

**Purpose:** JMH hit/miss/eviction/contention measurements.

### F121 --- `benchmarks/.../RateLimiterBenchmark.java`

**Purpose:** JMH acquisition throughput and contention measurements.

### F122 --- `benchmarks/.../LoadBalancerBenchmark.java`

**Purpose:** JMH selection cost by node count/thread count.

### F123 --- `examples/BasicSchedulingExample.java`

**Purpose:** consumer example for request construction, explicit
strategy, result handling.

### F124 --- `examples/CacheAndRateLimitExample.java`

**Purpose:** cache and limiter configuration, hit/miss and permit
decision handling.

### F125 --- `examples/ResilienceExample.java`

**Purpose:** bounded retry and circuit breaker usage; warns that retry
safety depends on operation semantics.

The file list has more than 80 planned files. Conditional files should
be omitted if their feature is deferred; files may be combined if no
separate responsibility justifies them.

------------------------------------------------------------------------

# 5. Function-level call graph

## 5.1 Scheduler path, function by function

1.  **F13 `schedule(request)`** is the consumer-facing contract.
2.  **F100 `schedule()`** coordinates the workflow; it contains no
    scheduling algorithm itself.
3.  **F37 `validateSchedulingRequest()`** checks required fields, IDs,
    durations, and policy-specific constraints.
4.  **F47 `normalize(request)`** creates immutable **F48
    `NormalizedJob`** values.
5.  **F49 `analyze(jobs)`** creates **F27 `WorkloadProfile`**.
6.  **F56 `resolve(profile,constraints,mode)`** queries **F55
    registry**, filters with **F50 constraints**, and invokes **F57
    policy** only for automatic selection.
7.  The selected implementation follows **F62 `SchedulingAlgorithm`**
    and is one of F64--F68.
8.  **F63 `validateSchedule(output,input,options)`** checks invariants.
9.  **F51 `JobMetricsCalculator`** calculates defined metrics from F70
    timeline.
10. **F100** builds **F26 `SchedulingResult`** and calls F109 metrics
    recorder.
11. Result returns to the consumer.

**Reason for boundaries:** algorithm rules can change without rewriting
API validation, selection policy, or result formatting.

## 5.2 Strategy explanation path

`F13 explain()` → `F100 explain()` → validation/normalization →
`F49 analyze()` → `F56 resolve()` → `F58 build()` →
`F28 StrategyExplanation`.

This path must not execute real tasks or claim that the chosen algorithm
is empirically optimal.

## 5.3 Execution path

`F14 submit()` → `F101 submit()` → `F38 validate()` → `F41 ID` →
`F97 TaskEnvelope` → `F93 BoundedTaskExecutor` → worker invokes callback
→ `F95` validates state transitions → `F94` stores bounded snapshot →
F109 metrics.

`cancel(id)` → F94 lookup → F96 cancellation request/Future interruption
where applicable → F95/F94 state update. Cancellation is best-effort for
arbitrary user code.

`shutdown(mode,timeout)` → F42 lifecycle close gate → F93 shutdown → F40
deadline/remaining wait → final service state.

If the queue is full, return/reject according to explicit configuration.
Do not silently execute rejected work on the caller thread unless that
is an explicit selected policy.

## 5.4 Cache path

`get(key)` → F102 → F72 storage lookup → F75 expiry check if enabled →
F71 policy hook on hit → F76 stats → typed result.

`put(key,value)` → validate → F72 replacement/capacity check → F71
eviction candidate → remove old entry → insert new entry → F76 counters.

F72 owns storage and capacity. F73/F74 own eviction ordering. F75 owns
expiry calculation. Do not duplicate capacity management inside each
policy.

## 5.5 Rate-limit path

`tryAcquire(n)` → F103 validates positive permits → F39 monotonic time →
F78 refill/accounting → F33 granted/denied decision → F109 metric.

Timed waiting additionally creates F40 deadline and uses an
interruption-aware wait mechanism. The token bucket calculates permit
availability; the runtime method controls waiting semantics.

## 5.6 Retry path

`execute(operation,policy)` → F104 validates config → F40 deadline → F83
attempt loop → operation callback → F45 failure classification → F81
attempt cap/policy → F82 delay → wait within deadline → retry or
propagate final cause.

Stop when the operation succeeds, failure is non-retryable, attempt
limit is reached, deadline expires, or interruption/cancellation occurs.
Do not assume idempotency.

## 5.7 Circuit-breaker path

`executeProtected(operation)` → F105 → F86 engine → F85 `beforeCall()` →
callback only if admitted → F85 records success/failure and transitions
state → metrics.

`OPEN` rejects without calling the operation. `HALF_OPEN` admits only
the configured number of trials. The state machine owns transitions; the
engine owns the callback boundary.

## 5.8 Load-balancer path

`select(request)` → F106 → F87 snapshot → F88 eligibility filter → F89
selection interface → F90 or F91 → F35 result → metrics.

`updateNodes(nodes)` → F92 validation → F87 atomically publishes a new
immutable snapshot. No eligible nodes means an explicit no-node outcome,
not an invented fallback.

## 5.9 Metrics and diagnostics path

Runtime → F109 recorder → F110 bounded counters → F111 immutable
snapshot → F112 diagnostics → F36 health snapshot. Selected sanitized
events go through F113/F114.

Metrics and diagnostics must not call back into algorithm decisions. Do
not keep unbounded per-task metric dimensions or hold service locks
while formatting diagnostics.

------------------------------------------------------------------------

# 6. Algorithm selection and exact scope

## 6.1 Scheduling

-   **FCFS:** simple arrival-order selection; potential convoy effect.
-   **SJF:** shortest known/estimated burst; requires estimates and
    explicit arrival model.
-   **SRTF:** least remaining time; preemption/event simulation must be
    specified.
-   **Priority:** documented priority direction and stable ties; aging
    is separate.
-   **Round Robin:** ready-queue rotation and positive quantum; Phase 1
    is a simulation model unless live dispatch is separately designed.

## 6.2 Cache

-   **LRU:** access updates recency.
-   **FIFO:** access does not update insertion order.
-   **TTL:** optional expiry policy; use monotonic elapsed time and
    define lazy/background cleanup.
-   **LFU:** defer until frequency updates, tie-breaking, aging, and
    overhead are validated.

## 6.3 Rate limiting

-   **Token bucket:** preferred first implementation; explicit
    capacity/refill rate and O(1) accounting in normal conditions.
-   Fixed/sliding window may be considered later after boundary behavior
    and memory trade-offs are defined.
-   Initial limiter is in-process, not distributed across service
    instances.

## 6.4 Resilience

-   **Retry:** bounded attempts, explicit retryability, bounded backoff,
    deadline, cancellation, interruption preservation.
-   **Circuit breaker:** CLOSED/OPEN/HALF_OPEN state machine, threshold
    rules, open duration, bounded half-open trials.
-   Composition order must be documented because retry-inside-breaker
    and breaker-inside-retry produce different call counts and metrics.

## 6.5 Load balancing

-   **Round Robin:** first implementation candidate.
-   **Weighted Round Robin:** add when weight validation and
    distribution contract are stable.
-   **Least Connections:** defer until trustworthy connection/load
    measurements and ownership are available.

## 6.6 Deferred unless approved

Distributed locks/leader election, full virtual filesystem/storage
engine, kernel-level operations, mandatory framework integrations, LFU
before validation, least-connections without load signals, and claims of
globally optimal strategy selection.

------------------------------------------------------------------------

# 7. Reuse versus custom implementation policy

**Rule:** use a mature JDK primitive for the general mechanism;
implement the UnirCore-specific policy, contract, and adaptation. Add a
third-party library only when a documented requirement justifies it.

  ---------------------------------------------------------------------------------------------
  Need              Reuse                   Custom UnirCore logic          Third-party
                                                                           dependency
  ----------------- ----------------------- ------------------------------ --------------------
  Thread execution  `ThreadPoolExecutor`,   queue bounds, task             Not needed for core
                    `ExecutorService`,      metadata/state, rejection      
                    `BlockingQueue`,        mapping, lifecycle             
                    `Future`                                               

  Cancellation      `Future.cancel`,        best-effort cancellation       Not needed
                    interruption            contract and state updates     

  Priority/heap     `PriorityQueue` where   comparator, stable tie-break,  Not needed
                    suitable                scheduling model               

  Concurrent map    `ConcurrentHashMap`     compound-operation             Not needed
                                            synchronization and domain     
                                            semantics                      

  LRU               `LinkedHashMap`         capacity, concurrency, TTL,    Not needed
                    access-order where      public stats                   
                    suitable                                               

  FIFO              JDK map/deque           eviction policy and            Not needed
                    structures              consistency                    

  Timing            `System.nanoTime`,      deadline and policy semantics  Not needed
                    `Instant`, `Duration`                                  

  Resource permits  `Semaphore` or          lease ownership, timeout,      Not needed initially
                    `BlockingQueue`         accounting                     

  Token bucket      JDK atomic/lock         refill math and permit         Not needed
                    primitives              accounting                     

  Retry             `Callable`, JDK         retryability, attempts,        Not needed initially
                    time/interruption       backoff, deadline              

  Circuit breaker   atomics/locks           state machine, thresholds,     Not needed initially
                                            transitions                    

  Node selection    JDK collections/atomic  eligibility, weights, coherent Not needed
                    cursor                  snapshots                      

  Metrics           `LongAdder`,            bounded metric vocabulary and  Export adapter may
                    `AtomicLong`            snapshot semantics             be optional

  Logging           JDK logging or consumer avoid sensitive payloads; do   Do not force a
                    logging                 not log on every hot-path call logging facade

  Unit tests        selected framework such domain-specific tests and      Test-scope only
                    as JUnit Jupiter        contract assertions            

  Benchmarks        JMH                     workload setup and benchmark   Benchmark-only
                                            interpretation                 

  Distributed locks no simple local JDK     requires protocol/failure      Separate design;
                    primitive provides      model                          likely
                    distributed semantics                                  dependency/service

  Spring            Spring APIs in adapter  lifecycle/auto-configuration   Optional artifact
  integration                               adapter                        only
  ---------------------------------------------------------------------------------------------

### Rules that prevent poor dependency choices

-   Do not reimplement general-purpose executors, concurrent maps,
    queues, locks, clocks, UUIDs, or JMH.
-   Do not assume a `ConcurrentHashMap` makes a multi-step cache
    eviction operation atomic.
-   Do not use `PriorityQueue` as if it were thread-safe.
-   Do not use wall-clock time for TTL or elapsed timeout calculations.
-   Do not assume `Semaphore` implements token-bucket refill.
-   Do not blindly retry all exceptions or assume user callbacks are
    idempotent.
-   Do not make metrics export/framework adapters dependencies of core
    algorithms.
-   Do not import a large library for a few simple lines; do not
    hand-roll complex security/protocol systems merely to avoid
    dependencies.

------------------------------------------------------------------------

# 8. Relationship patterns

## 8.1 Facade/service

Public API F13--F20 → runtime F100--F107. Consumers depend on
interfaces, not `Default*` classes.

## 8.2 Strategy

F62 defines the scheduling contract; F64--F68 implement policies; F56
chooses a compatible implementation. Use an interface only when
implementations genuinely share a contract.

## 8.3 Registry/resolver/policy/explanation

F55 answers "what exists?"; F56 answers "what is compatible and
selected?"; F57 answers "which compatible choice does the documented
rule prefer?"; F58 explains the decision. Keep these responsibilities
separate.

## 8.4 Adapter/wrapper

F93 wraps JDK execution primitives; F99 may wrap `Semaphore`/queue
semantics; F110 wraps counters into a metrics contract. A wrapper must
add meaningful lifecycle, validation, state, or contract behavior.

## 8.5 State machine

F95 owns task transitions; F85 owns breaker transitions. Explicit
transition rules prevent impossible state combinations.

## 8.6 Immutable snapshots

F26/F27/F28/F31/F32/F35/F36/F111 represent immutable boundary data. A
snapshot can be internally consistent without being a global atomic
snapshot across all services.

## 8.7 Policy versus mechanism

-   Cache engine F72 owns storage/capacity; F73/F74 choose eviction.
-   Node registry F87 owns membership; F88 filters; F90/F91 select.
-   Retry engine F83 owns attempts; F82 delay; F45 failure
    classification.
-   Execution runtime F101 owns API/lifecycle; F93 runs tasks; F95
    validates transitions.

## 8.8 Ownership

-   A service closes an executor it creates, not one explicitly borrowed
    from its consumer.
-   Builders must make executor, clock, and metrics ownership clear.
-   Avoid mutable static registries/singletons.
-   Shared collaborators must document thread safety and lifetime.

------------------------------------------------------------------------

# 9. Dependency direction

  ---------------------------------------------------------------------------------------------------------
  Package                 May depend on                                             Must not depend on
  ----------------------- --------------------------------------------------------- -----------------------
  `api`                   JDK types and stable API values                           runtime
                                                                                    implementations,
                                                                                    frameworks

  `core`                  JDK and low-level contracts                               service
                                                                                    implementations,
                                                                                    framework types

  `workload`              core and public request models                            runtime, exporters

  `algorithms`            JDK, normalized models, algorithm contracts               orchestration, runtime,
                                                                                    frameworks

  `strategy`              core/workload/capability/SPI contracts                    concrete runtime
                                                                                    classes

  `orchestration`         API/core/workload/strategy/algorithm contracts            framework types

  `execution`             JDK concurrency and lifecycle contracts                   unrelated scheduling
                                                                                    policies

  `runtime`               API/core/workload/strategy/algorithms/execution/metrics   optional integration
                                                                                    package

  `metrics`               JDK and metrics contracts                                 callbacks that control
                                                                                    algorithm decisions

  `diagnostics`           snapshots, lifecycle, metrics                             mutation of algorithm
                                                                                    internals

  `spi`                   stable API/core/JDK                                       runtime implementation
                                                                                    classes

  `integrations`          API/SPI plus the optional framework                       core importing
                                                                                    framework types

  tests                   production classes and test libraries                     production depending on
                                                                                    test classes

  benchmarks              algorithms/services and JMH                               benchmark classes in
                                                                                    runtime JAR
  ---------------------------------------------------------------------------------------------------------

If a cycle appears, move the smallest shared contract inward. Do not
solve cycles with a giant `CommonUtils` class.

------------------------------------------------------------------------

# 10. File relationship index

  -----------------------------------------------------------------------------
  Source                    Depends on/calls        Why
  ------------------------- ----------------------- ---------------------------
  F13 Scheduler             F100                    public contract to
                                                    implementation

  F100 DefaultScheduler     F37, F47, F49, F56,     validate, normalize,
                            F62, F63, F51, F109     analyze, resolve, run,
                                                    verify, measure

  F47 JobNormalizer         F25, F48, F43           validated request to
                                                    normalized jobs

  F49 WorkloadAnalyzer      F48, F27                derive selection features

  F56 StrategyResolver      F50, F55, F57, F58      compatibility, candidates,
                                                    rule, explanation

  F62 SchedulingAlgorithm   F48, F69, F52           algorithm
                                                    contract/input/options/ID

  F64--F68 scheduling       F62, F69, F70           distinct scheduling rules
  algorithms                                        

  F63 ScheduleValidator     F70 and request         verify output before public
                            invariants              result

  F15 Cache                 F102                    public cache contract

  F102 DefaultCache         F72, F71, F73/F74, F75, storage, policy, expiry,
                            F76                     stats

  F16 RateLimiter           F103                    public limiter contract

  F103 DefaultRateLimiter   F78, F39, F40, F33,     policy, time, deadline,
                            F109                    result, metrics

  F17 RetryExecutor         F104                    public retry contract

  F104 DefaultRetryExecutor F81--F83, F45, F40,     policy, delay, loop,
                            F109                    classifier, deadline

  F18 CircuitBreaker        F105                    public breaker contract

  F105                      F84--F86, F109          config, state machine,
  DefaultCircuitBreaker                             callback boundary

  F19 LoadBalancer          F106                    public load-balancer
                                                    contract

  F106 DefaultLoadBalancer  F87--F92, F35, F109     validate, snapshot, filter,
                                                    select, result

  F14 ExecutionService      F101                    public execution contract

  F101                      F38, F41, F42,          validate, ID, lifecycle,
  DefaultExecutionService   F93--F97, F109          execute, track, cancel

  F93 BoundedTaskExecutor   JDK                     reuse worker and queue
                            executor/queue/Future   mechanisms

  F95 TaskStateMachine      F94/F31                 validate and publish task
                                                    transitions

  F20 ResourceManager       F107                    public resource contract,
                                                    if approved

  F107                      F98/F99/F40             lease, pool, timeout
  DefaultResourceManager                            

  F109 MetricsRecorder      F110                    metrics abstraction

  F110                      F111                    bounded counters to
  InMemoryMetricsRecorder                           snapshot

  F112 DiagnosticsService   F111/F113/F114/F36      health/diagnostic snapshot

  F108 UnirCoreFactory      F100--F107 and shared   dependency assembly and
                            collaborators           ownership

  F115 ControlledTimeSource F39                     deterministic time-based
                                                    tests

  F118 ContractAssertions   public                  common assertions
                            contracts/algorithm     
                            implementations         

  F119--F122 benchmarks     selected                isolated performance
                            algorithms/services +   measurement
                            JMH                     
  -----------------------------------------------------------------------------

------------------------------------------------------------------------

# 11. Function-level inventory by service

## 11.1 Scheduler functions

-   `schedule(request)`: validate, normalize, analyze, resolve, run,
    verify, calculate metrics, return result.
-   `selectStrategy(profile)`: return compatible strategy without
    running a schedule.
-   `estimate(request)`: return explicitly defined estimates, not actual
    runtime.
-   `explain(request)`: return candidate filtering and rule identifiers.
-   `validate(request)`: validate only.
-   `normalize(request)`: convert external job data to internal
    validated jobs.
-   `analyze(jobs)`: derive workload features.
-   `resolve(profile,constraints,mode)`: filter and choose strategy.
-   `validateSchedule(output)`: enforce output invariants.
-   `calculateMetrics(timeline)`: calculate defined
    waiting/turnaround/response values.
-   `recordMetrics(result)`: record bounded operational observations.

## 11.2 Execution functions

-   `submit(request)`: validate and enqueue or reject.
-   `submitAll(requests)`: apply documented batch acceptance semantics.
-   `cancel(taskId)`: request cooperative cancellation.
-   `inspect(taskId)`: return immutable snapshot or explicit not-found
    outcome.
-   `shutdown(mode,timeout)`: stop admission and drain/cancel within
    deadline.
-   `runEnvelope(envelope)`: execute callback and capture
    success/failure.
-   `transition(taskId,state)`: validate legal transition.
-   `recordCompletion(taskId,outcome)`: update bounded history and
    metrics.

## 11.3 Cache functions

-   `get(key)`: lookup, expiry, recency update, hit/miss record.
-   `put(key,value)`: insert/replace and enforce capacity.
-   `putIfAbsent(key,value)`: atomic insert if no live value exists.
-   `invalidate(key)` / `invalidateAll()`: remove entries and policy
    state consistently.
-   `evictIfRequired()`: enforce capacity using policy-selected
    candidates.
-   `isExpired(entry,now)`: TTL check using monotonic time.
-   `stats()`: immutable stats snapshot.
-   `close()`: only if cache owns a background resource.

## 11.4 Rate-limiter functions

-   `tryAcquire(permits)`: immediate decision.
-   `acquire(permits)`: waiting behavior only if contract says so.
-   `tryAcquire(permits,timeout)`: wait up to deadline.
-   `refill(now)`: token bucket accounting.
-   `availablePermits(now)`: inspect current state.
-   `state()`: immutable state snapshot.
-   `validatePermits(permits)`: reject invalid counts.
-   `computeWaitNanos(missing)`: bounded estimate; handle overflow and
    zero refill.

## 11.5 Retry functions

-   `execute(operation,policy)`: public entry.
-   `executeAttempt(operation,index)`: run one attempt.
-   `isRetryable(failure,policy)`: apply explicit classifier.
-   `delayForAttempt(index)`: bounded backoff.
-   `shouldRetry(index,failure,deadline)`: combine cap, classifier,
    cancellation, deadline.
-   `awaitDelay(delay,deadline)`: honor interruption and deadline.
-   `propagateFinalFailure(failure)`: preserve cause and useful context.

## 11.6 Circuit-breaker functions

-   `beforeCall()`: admit, reject, or grant half-open trial.
-   `recordSuccess()` / `recordFailure(failure)`: update counters and
    transitions.
-   `transitionToOpen(now)` / `transitionToHalfOpen(now)` /
    `transitionToClosed()`: explicit state transitions.
-   `state()`: state snapshot.
-   `reset()`: documented administrative reset.
-   `executeProtected(operation)`: callback boundary plus outcome
    recording.

## 11.7 Load-balancer functions

-   `updateNodes(nodes)`: validate and atomically publish snapshot.
-   `snapshotNodes()`: read coherent node view.
-   `eligibleNodes(request,nodes)`: filter.
-   `select(nodes)`: call chosen strategy.
-   `selectRoundRobin(nodes)`: advance cursor safely.
-   `selectWeightedRoundRobin(nodes)`: apply validated weights.
-   `inspect()`: snapshot without changing selection state.
-   `validateNodeSet(nodes)`: identity/weight validation.

## 11.8 Strategy resolver functions

-   `register(descriptor,provider)`: register unique strategy.
-   `candidates(family)`: list implementations.
-   `filterByCapabilities(candidates,constraints)`: remove incompatible
    candidates.
-   `resolveExplicit(id,candidates)`: validate exact consumer choice.
-   `resolveAutomatic(profile,candidates)`: deterministic published
    rules.
-   `explainSelection(decision)`: selected/rejected IDs and reasons.
-   `validateConfiguration(strategy,config)`: reject invalid config
    before use.

------------------------------------------------------------------------

# 12. Allowed and prohibited call relationships

**Allowed** - Public service → runtime implementation → validator →
algorithm contract → algorithm. - Runtime → metrics recorder (one-way
observation). - Runtime → bounded diagnostics sink. - Resolver →
descriptors/capabilities → selection policy. - Algorithm → normalized
input → algorithm result. - Optional integration → public API/SPI. -
Tests → production code.

**Prohibited** - Algorithm calling `DefaultScheduler` or another runtime
service. - Metrics callbacks changing algorithm decisions. - Public API
importing concrete `Default*` implementations. - Core importing
Spring/cloud framework types. - Retry engine assuming every exception is
transient. - Load balancer inventing an eligible node. - Resolver
silently replacing an explicit strategy. - Algorithm printing to console
or requiring a logging backend. - Test/benchmark dependencies in the
runtime artifact. - Unbounded task-ID metric labels or diagnostic maps.

------------------------------------------------------------------------

# 13. Implementation order

### Stage A --- Contracts and foundation

1.  F01--F11 build/docs skeleton.
2.  F21--F36 public exceptions and value types.
3.  F37--F46 validation/time/deadline/lifecycle helpers.
4.  Compile a minimal consumer example.

### Stage B --- Scheduling vertical slice

5.  F47--F51 workload normalization and metric calculation.
6.  F52--F61 strategy metadata, resolver, explanation, SPI.
7.  F62--F70 scheduling contract and chosen algorithms.
8.  F100 scheduler runtime and contract tests.

Start with a small subset. Add SJF/SRTF only after arrival/preemption
semantics are specified.

### Stage C --- Execution lifecycle

9.  F93--F97 task executor, registry, state, cancellation.
10. F101 execution runtime.
11. Test saturation, cancellation races, and shutdown.

### Stage D --- Cache and rate limiting

12. F71--F76 cache engine/policies.
13. F102 cache runtime.
14. F77--F80 token bucket.
15. F103 rate-limit runtime.

### Stage E --- Resilience and load balancing

16. F81--F86 retry and circuit breaker.
17. F104/F105 runtime services.
18. F87--F92 node registry and selection.
19. F106 runtime service.

### Stage F --- Observability and release

20. F98/F99/F107 only if resource leasing is approved.
21. F109--F114 metrics/diagnostics.
22. F108 factory assembly.
23. F115--F125 test support, benchmarks, and examples.
24. Run package/API compatibility checks and release candidate
    validation.

## 14. Acceptance criteria

-   [ ] Public API names and contracts are agreed before coding.
-   [ ] Scheduling is explicitly simulation/planning or live dispatch.
-   [ ] Cache capacity, null, TTL, and concurrency behavior are
    specified.
-   [ ] Rate-limit denial and waiting semantics are specified.
-   [ ] Retry attempt counting, idempotency warning, deadlines, and
    cancellation are specified.
-   [ ] Circuit-breaker transitions and retry composition are specified.
-   [ ] Load-balancer eligibility, weight, and update semantics are
    specified.
-   [ ] Execution queue-full, task-state, cancellation, and shutdown
    behavior are specified.
-   [ ] Dependency direction is acyclic.
-   [ ] Every third-party dependency has a written justification.
-   [ ] Each stable algorithm has contract and boundary tests.
-   [ ] Metrics/diagnostics retention is bounded.
-   [ ] No conditional feature is represented by an empty placeholder.
-   [ ] Public methods have JavaDoc and a real consumer purpose.
-   [ ] Benchmark claims are backed by reproducible JMH results.
-   [ ] Published examples compile against the actual packaged artifact.

## Final rule

Every file must own a clear responsibility. Every function must have a
testable contract. Every call edge must have a reason. Reuse JDK
primitives for generic mechanisms, implement UnirCore-specific policies
yourself, and add third-party libraries only when the required behavior
genuinely justifies the dependency.
