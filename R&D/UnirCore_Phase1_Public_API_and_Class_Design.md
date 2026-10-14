# UnirCore Phase 1 --- Public API, Class Design and Consumer Contract

**Status:** Proposed Phase-1 specification for design review before
implementation\
**Product:** UnirCore --- reusable Java systems and OS-inspired
algorithms\
**Distribution:** Maven Central\
**Audience:** Library maintainers and developers integrating UnirCore
into applications

> **Design status:** The APIs and signatures below are proposed
> contracts, not claims about already implemented classes. Finalize the
> public surface before coding; only advertise features that have a
> tested implementation.

## 1. Product purpose

UnirCore gives Java applications reusable, configurable implementations
of systems-oriented algorithms and runtime services: task scheduling,
bounded execution, cache policies, rate limiting, retries, circuit
breakers, resource capacity management, load balancing, and selected
coordination/data structures.

The consumer should be able to configure a service, make a
purpose-oriented call, receive a typed result, inspect diagnostics when
useful, and close resources the service owns. They should not need to
know how UnirCore's planner, resolver, worker dispatcher, or algorithm
registry works internally.

UnirCore is a **library running inside the consumer's JVM**, not a
hosted platform. Its core should not require a UnirCore server, cloud
account, database, or external API.

### Scope boundaries

UnirCore implements application-level algorithms inspired by OS and
system-design concepts. It does not replace the operating system
kernel's actual CPU scheduler, physical memory manager, or hardware disk
scheduler. A single-JVM object is not automatically distributed across
machines. No algorithm is universally fastest for every workload.

## 2. Phase-1 scope

  -----------------------------------------------------------------------
  Domain                  Proposed initial        Consumer benefit
                          capability              
  ----------------------- ----------------------- -----------------------
  Scheduling              FCFS, SJF, SRTF,        Calculate or compare
                          priority, round-robin;  task ordering using a
                          rule-based strategy     consistent model
                          selection where         
                          justified               

  Execution               Bounded task            Control work and avoid
                          submission, queueing,   unbounded queues
                          task status,            
                          cancellation and        
                          shutdown                

  Cache                   LRU, FIFO and TTL       Reuse values with clear
                          expiry; add LFU only    eviction and expiration
                          after validation        behaviour

  Rate limiting           At least one            Limit requests to APIs
                          well-tested policy,     or shared resources
                          such as token bucket    

  Resilience              Bounded retry/backoff;  Handle transient
                          circuit breaker only    failures and avoid
                          after its state model   repeatedly calling
                          is tested               unhealthy dependencies

  Resource management     Capacity limits and     Bound access to a
                          acquire/release leases  limited resource

  Load balancing          Round-robin/weighted    Select a target
                          policies when valid     consistently
                          node information is     
                          supplied                

  Metrics/diagnostics     Counters, timers,       Understand latency,
                          snapshots and strategy  rejection and strategy
                          explanations for        decisions
                          supported services      

  SPI                     Stable extension points Add custom
                          for selected strategy   implementations without
                          contracts               changing UnirCore
  -----------------------------------------------------------------------

Defer a production distributed lock/leader-election service, full
virtual filesystem, kernel-level operations, mandatory framework
integrations, and claims of globally optimal automatic strategy
selection unless their requirements and implementations are separately
approved.

## 3. Consumer workflow

``` text
Choose a purpose
    -> create configuration/request
    -> call a domain API
    -> validate input and constraints
    -> select explicit strategy or documented automatic policy
    -> run the algorithm (and runtime if needed)
    -> return typed result and relevant timing/status
    -> optionally inspect metrics/diagnostics
    -> close services/resources owned by the consumer
```

Not every call needs every stage. A cache lookup should not pass through
a general workload planner. Pure scheduling calculations should not
unexpectedly execute user tasks.

### Example: bounded execution

``` java
ExecutionService execution = UnirCore.execution(
    ExecutionConfig.builder()
        .workers(4)
        .queueCapacity(200)
        .build()
);

TaskHandle<String> handle = execution.submit(
    TaskRequest.of(() -> "processed")
        .executionTimeout(Duration.ofSeconds(2))
);

// Use the handle's documented completion API.
// Close the service when the application no longer needs it.
execution.close();
```

This is illustrative. For asynchronous submission, `submit` should
normally return promptly with a task handle/future, not block until
completion. The exact handle API must state how a result is retrieved
and whether retrieval blocks.

### Example: cache

``` java
Cache<String, UserProfile> cache = UnirCore.cache(
    CacheConfig.<String, UserProfile>builder()
        .maximumSize(10_000)
        .evictionPolicy(EvictionPolicy.LRU)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build()
);

cache.put("user:42", profile);
Optional<UserProfile> value = cache.get("user:42");
```

The cache contract must define expiration semantics and null handling.
Recommended default: reject null keys and values.

### Example: rate limiter

``` java
RateLimiter limiter = UnirCore.rateLimiter(
    RateLimitConfig.builder()
        .algorithm(RateLimitAlgorithm.TOKEN_BUCKET)
        .capacity(100)
        .refillRate(50, Duration.ofSeconds(1))
        .build()
);

RateLimitDecision decision = limiter.acquire(1);
if (decision.allowed()) {
    // Continue the protected operation.
} else {
    Duration retryAfter = decision.retryAfter();
}
```

`acquire` should be non-blocking and return a decision. A potentially
blocking wait belongs in a separately named method with an explicit
timeout.

### Example: retry

``` java
RetryPolicy policy = RetryPolicy.builder()
    .maxAttempts(3) // Includes the first attempt.
    .initialDelay(Duration.ofMillis(100))
    .maxDelay(Duration.ofSeconds(2))
    .backoff(Backoff.EXPONENTIAL)
    .jitter(true)
    .build();

RetryResult<Response> result =
    retryExecutor.execute(policy, () -> client.call());
```

Document which failures are retryable and whether the operation must be
idempotent. Retrying non-idempotent operations may duplicate side
effects.

## 4. Public API design rules

1.  **Purpose-oriented:** Consumers call `cache`, `rateLimiter`,
    `scheduler`, `execution`, or `retry`, not implementation helpers.
2.  **Interfaces first:** Consumers depend on stable interfaces;
    internal implementations can evolve.
3.  **Typed requests/results:** Avoid raw `Object`, ambiguous booleans,
    and dozens of nullable fields in a universal request class.
4.  **Immutable configuration:** Use validated builders for
    multi-parameter services.
5.  **Validate early:** Reject invalid capacities, negative durations,
    missing required values and incompatible constraints before
    execution.
6.  **Explicit timing:** Distinguish queue wait, execution timeout,
    deadline, retry delay, TTL and shutdown grace period.
7.  **Clear ownership:** Document who owns worker threads, timers and
    queues, and who must close them.
8.  **Thread-safety contract:** Every service states whether it is
    thread-safe, thread-confined, immutable or not thread-safe.
9.  **Framework-independent core:** Spring, logging and metrics adapters
    remain optional.
10. **Explain automatic decisions:** Report the selected strategy and a
    concise reason when meaningful.
11. **No false optimality:** "Automatic" means a documented rule-based
    decision, not a guarantee of a globally optimal strategy.
12. **Expected decisions are results:** Rate-limit denial or an open
    circuit should be typed outcomes, not generic exceptions.

## 5. Proposed public entry point

Keep the facade small and do not expose every algorithm through one
enormous static class.

``` java
public final class UnirCore {
    private UnirCore() {}

    public static Scheduler scheduler(SchedulerConfig config);
    public static ExecutionService execution(ExecutionConfig config);
    public static ResourceManager resourceManager(ResourceConfig config);
    public static <K, V> Cache<K, V> cache(CacheConfig<K, V> config);
    public static RateLimiter rateLimiter(RateLimitConfig config);
    public static RetryExecutor retryExecutor(RetryPolicy policy);
    public static CircuitBreaker circuitBreaker(CircuitBreakerConfig config);
    public static LoadBalancer loadBalancer(LoadBalancerConfig config);
}
```

Only expose factories for capabilities actually implemented and tested
in Phase 1. The public API consists of documented interfaces,
configuration/request models, result types, enums, supported exceptions
and stable SPI contracts. Planners, algorithm nodes, internal queues,
worker dispatchers and mutable registries remain internal.

## 6. Primary public API catalogue: inputs and outputs

Target approximately **35 primary operations** across the first
supported domains. This is a proposed inventory for review, not a
promise that every call belongs in the first release. Accessors, builder
setters and lifecycle helpers are not counted.

### 6.1 Scheduling --- 5 operations

  -----------------------------------------------------------------------------------------------------
  Call                        Input             Output                  Purpose
  --------------------------- ----------------- ----------------------- -------------------------------
  `schedule(request)`         Tasks, policy,    `ScheduleResult`        Produce a task ordering/plan
                              constraints                               

  `selectStrategy(profile)`   Workload profile  `StrategySelection`     Select a supported policy

  `estimate(request)`         Task estimates    `ScheduleEstimate`      Calculate
                              and scheduling                            waiting/turnaround/completion
                              inputs                                    estimates when possible

  `explain(selection)`        Selected strategy `DecisionExplanation`   Explain the decision

  `validate(request)`         Scheduling        `ValidationResult`      Report invalid or incompatible
                              request                                   constraints
  -----------------------------------------------------------------------------------------------------

Inputs may include task ID, arrival time, estimated burst/duration,
priority and optional deadline. Outputs may include ordered tasks,
start/finish/wait/turnaround estimates, selected policy and validation
messages. Pure scheduling calculations must not execute the tasks unless
a separate execution API is explicitly used.

### 6.2 Execution and concurrency --- 5 operations

  ----------------------------------------------------------------------------------------
  Call                        Input               Output                 Purpose
  --------------------------- ------------------- ---------------------- -----------------
  `submit(task)`              Callable/Runnable   `TaskHandle<T>`        Submit work
                              and optional                               
                              metadata                                   

  `submitAll(tasks)`          Collection of task  Collection of handles  Submit a batch
                              requests                                   

  `cancel(taskId)`            Task ID             `CancellationResult`   Request
                                                                         cancellation

  `inspect(taskId)`           Task ID             `TaskStatus`           Inspect lifecycle
                                                                         state

  `shutdown(mode, timeout)`   Shutdown mode and   `ShutdownResult`       Stop accepting
                              grace period                               work and manage
                                                                         in-flight tasks
  ----------------------------------------------------------------------------------------

A handle may expose `id()`, `status()`, `cancel()` and a completion
stage. If a blocking wait is offered, name and document it explicitly.
Cancellation of arbitrary Java code is generally
cooperative/best-effort.

### 6.3 Cache --- 6 operations

  --------------------------------------------------------------------------------------
  Call                        Input             Output                 Purpose
  --------------------------- ----------------- ---------------------- -----------------
  `get(key)`                  Key               `Optional<V>` or       Read non-expired
                                                lookup result          value

  `put(key, value)`           Key/value         `CacheWriteResult`     Insert/update

  `putIfAbsent(key, value)`   Key/value         Insert/current-value   Avoid replacing
                                                outcome                an existing entry

  `invalidate(key)`           Key               Removal outcome        Remove one entry

  `invalidateAll()`           None              `CacheClearResult`     Clear cache

  `stats()`                   None              `CacheStats`           Hits, misses,
                                                                       evictions,
                                                                       expirations and
                                                                       size
  --------------------------------------------------------------------------------------

Define whether TTL starts on write or access and whether expiration is
checked on access or maintained in the background. Expired values must
never be returned as valid. Document whether statistics are approximate
under concurrent use.

### 6.4 Rate limiting --- 3 operations

  ------------------------------------------------------------------------------------------
  Call                             Input             Output                Purpose
  -------------------------------- ----------------- --------------------- -----------------
  `acquire(permits)`               Permit count      `RateLimitDecision`   Immediately allow
                                                                           or reject

  `tryAcquire(permits, timeout)`   Permit count and  Decision/result       Wait only within
                                   max wait                                a bound

  `state()`                        None              `RateLimiterState`    Inspect
                                                                           capacity/refill
                                                                           state
  ------------------------------------------------------------------------------------------

The default call should not sleep. Include `retryAfter` only when it can
be calculated reliably.

### 6.5 Resilience --- 6 operations

  --------------------------------------------------------------------------------------------
  Call                            Input             Output             Purpose
  ------------------------------- ----------------- ------------------ -----------------------
  `execute(operation)`            Callable plus     `RetryResult<T>`   Execute with bounded
                                  retry policy                         retries

  `nextDelay(attempt)`            Attempt number    `Duration`         Calculate backoff

  `executeProtected(operation)`   Operation plus    Typed operation    Apply circuit-breaker
                                  breaker policy    result             admission and record
                                                                       outcome

  `state()`                       None              `CircuitState`     Read
                                                                       closed/open/half-open
                                                                       state

  `reset()`                       None              Reset outcome      Administrative reset if
                                                                       supported and safe

  `config()`                      None              Immutable config   Inspect active policy
                                                    view               
  --------------------------------------------------------------------------------------------

Prefer `executeProtected` over forcing every consumer to call
`allowRequest()`, execute, then remember to record success/failure.
Low-level recording methods may be SPI/internal APIs if necessary. Retry
and circuit breaking are distinct: retry repeats eligible failures; a
circuit breaker rejects calls when failure state indicates the
dependency should be given time to recover.

### 6.6 Resource management --- 3 operations

  -----------------------------------------------------------------------------------
  Call                       Input             Output               Purpose
  -------------------------- ----------------- -------------------- -----------------
  `acquire(type, timeout)`   Resource type and `ResourceLease` or   Acquire bounded
                             timeout           typed failure        capacity

  `release(lease)`           Lease/token       Release result       Return capacity

  `inspect(type)`            Resource type     `ResourceSnapshot`   Inspect capacity,
                                                                    usage and waiters
  -----------------------------------------------------------------------------------

A lease should identify ownership and help prevent double release.
Define blocking behaviour, timeout semantics and try-with-resources
support.

### 6.7 Load balancing --- 3 operations

  --------------------------------------------------------------------------------
  Call                Input             Output                   Purpose
  ------------------- ----------------- ------------------------ -----------------
  `select(request)`   Candidate nodes   `NodeSelection`          Choose a target
                      and request                                
                      metadata                                   

  `update(nodes)`     Current candidate Update result            Refresh targets
                      set                                        

  `inspect()`         None              `LoadBalancerSnapshot`   Inspect nodes and
                                                                 selection
                                                                 statistics
  --------------------------------------------------------------------------------

Start with round-robin and weighted round-robin if implemented
correctly. Least-connections requires reliable connection/load data.
UnirCore cannot infer remote health without a configured health source.

### 6.8 Metrics and diagnostics --- 4 operations

  -----------------------------------------------------------------------------------
  Call                   Input              Output                  Purpose
  ---------------------- ------------------ ----------------------- -----------------
  `snapshot(scope)`      Optional           `MetricsSnapshot`       Read configured
                         scope/filter                               metrics

  `explain(requestId)`   Decision/request   `DecisionExplanation`   Explain a
                         ID                                         recorded decision

  `health()`             None               `HealthReport`          Report service
                                                                    status

  `resetStats(scope)`    Optional scope     Reset outcome           Reset supported
                                                                    counters
  -----------------------------------------------------------------------------------

Metrics must be configurable where overhead matters. Core should not
require a third-party metrics framework.

**Count:** 5 + 5 + 6 + 3 + 6 + 3 + 3 + 4 = **35 proposed primary
operations**. Some may be merged or deferred after API review. Do not
ship an operation without implementation, tests and documented
semantics.

## 7. Request and result contracts

Do not force every domain into a single request class. Use
domain-specific immutable request/configuration types, with shared
metadata only where meaningful.

A request may include a correlation ID, required data,
explicit/automatic strategy preference, constraints, timeout/deadline
and optional diagnostic metadata. Validate static constraints when
building configuration and runtime-dependent constraints when invoking a
service.

A result should contain only fields meaningful for its domain. Shared
metadata may include status, request ID, elapsed duration, selected
strategy, warnings and diagnostic references.

Illustrative contract:

``` java
public interface OperationResult {
    OperationStatus status();
    Duration elapsed();
    Optional<String> requestId();
    List<DiagnosticMessage> diagnostics();
}
```

Avoid `null` as a hidden representation of "not found", "rejected",
"timed out" or "failed". Use `Optional`, typed decisions or documented
result statuses. Expected outcomes such as rate-limit rejection should
not be generic exceptions.

## 8. Class diagram --- public view

``` mermaid
classDiagram
    class ConsumerApplication
    class UnirCore {
      +scheduler(config)
      +execution(config)
      +cache(config)
      +rateLimiter(config)
      +retryExecutor(policy)
      +circuitBreaker(config)
      +resourceManager(config)
      +loadBalancer(config)
    }
    class Scheduler {
      <<interface>>
      +schedule(request) ScheduleResult
      +selectStrategy(profile) StrategySelection
      +validate(request) ValidationResult
    }
    class ExecutionService {
      <<interface>>
      +submit(task) TaskHandle
      +submitAll(tasks)
      +cancel(taskId) CancellationResult
      +inspect(taskId) TaskStatus
      +shutdown(mode, timeout) ShutdownResult
    }
    class Cache {
      <<interface>>
      +get(key) Optional
      +put(key, value) CacheWriteResult
      +putIfAbsent(key, value)
      +invalidate(key)
      +invalidateAll()
      +stats() CacheStats
    }
    class RateLimiter {
      <<interface>>
      +acquire(permits) RateLimitDecision
      +tryAcquire(permits, timeout) RateLimitDecision
      +state() RateLimiterState
    }
    class RetryExecutor {
      <<interface>>
      +execute(operation) RetryResult
      +nextDelay(attempt) Duration
    }
    class CircuitBreaker {
      <<interface>>
      +executeProtected(operation) Result
      +state() CircuitState
      +reset() ResetResult
    }
    class ResourceManager {
      <<interface>>
      +acquire(type, timeout) ResourceLease
      +release(lease) ReleaseResult
      +inspect(type) ResourceSnapshot
    }
    class LoadBalancer {
      <<interface>>
      +select(request) NodeSelection
      +update(nodes) UpdateResult
      +inspect() LoadBalancerSnapshot
    }
    ConsumerApplication --> UnirCore
    UnirCore ..> Scheduler
    UnirCore ..> ExecutionService
    UnirCore ..> Cache
    UnirCore ..> RateLimiter
    UnirCore ..> RetryExecutor
    UnirCore ..> CircuitBreaker
    UnirCore ..> ResourceManager
    UnirCore ..> LoadBalancer
```

## 9. Class diagram --- internal request handling

``` mermaid
flowchart TD
    A[Consumer API call] --> B[Request/configuration]
    B --> C[Validation and normalization]
    C -->|Invalid| X[Validation failure]
    C -->|Valid| D[Workload analysis if needed]
    D --> E[Constraint/capability checks]
    E --> F[Strategy resolver]
    F --> G[Explicit strategy or automatic rule]
    G --> H[Algorithm implementation]
    H --> I[Execution/runtime if needed]
    I --> J[Typed result]
    H --> K[Metrics and diagnostics]
    I --> K
    J --> L[Consumer]
    K --> M[Optional inspection API]
```

Responsibilities: API accepts purpose-specific calls; core models hold
requests/results; validator checks ranges and combinations; workload
analyzer derives useful characteristics; capability checks exclude
incompatible strategies; resolver chooses a documented strategy;
algorithms do the domain work; runtime manages asynchronous/stateful
work; metrics/diagnostics capture configured observations. Simple
operations can skip irrelevant stages.

## 10. Preserve the agreed 14 principal areas

The agreed hierarchy is retained below. These are logical Java packages
under `src/main/java`; use the verified Java namespace rather than
treating these as root-level folders.

``` text
src/main/java/<verified-package>/unircore/
├── api/
│   ├── scheduler/  ├── execution/  ├── resource/  ├── memory/
│   ├── cache/      ├── storage/    ├── filesystem/ ├── traffic/
│   ├── resilience/ └── distributed/
├── core/
│   ├── model/  ├── request/  ├── result/  ├── context/
│   ├── lifecycle/  ├── metadata/  └── exception/
├── orchestration/
│   ├── orchestrator/  ├── planner/  ├── resolver/  ├── analyzer/
│   ├── validator/  ├── constraint/  └── decision/
├── strategy/
│   ├── registry/  ├── factory/  ├── descriptor/
│   ├── capability/  ├── policy/  └── provider/
├── workload/
│   ├── profile/  ├── analyzer/  ├── classifier/
│   ├── characteristic/  └── requirement/
├── algorithms/
│   ├── scheduling/  ├── synchronization/  ├── deadlock/
│   ├── memory/  ├── paging/  ├── disk/  ├── filesystem/
│   ├── hashing/  ├── rate/  ├── resilience/  ├── distributed/
│   └── collections/
├── execution/
│   ├── engine/  ├── worker/  ├── queue/  ├── dispatcher/
│   ├── executor/  └── lifecycle/
├── runtime/
│   ├── scheduler/  ├── process/  ├── resource/  ├── memory/
│   ├── cache/  ├── storage/  ├── filesystem/  ├── traffic/
│   ├── network/  ├── resilience/  └── distributed/
├── metrics/
│   ├── collector/  ├── counter/  ├── timer/  ├── histogram/
│   ├── registry/  └── snapshot/
├── diagnostics/
│   ├── decision/  ├── trace/  ├── health/  └── statistics/
├── spi/
│   ├── strategy/  ├── algorithm/  ├── execution/
│   ├── metrics/  └── extension/
├── integrations/
│   ├── spring/  ├── executor/  └── standard/
└── testing/
    ├── fixtures/  ├── generators/  ├── assertions/  └── stress/
```

This gives **13 principal Java package roots**. The previously agreed
**14 principal project areas** count `benchmarks/` as the fourteenth
area; `examples/`, `docs/`/`documentation/`, and optional `build/` are
repository-support directories, not extra runtime API packages:

``` text
benchmarks/
examples/
docs/                 # or documentation/; choose one name
build/                # only if build support files need a dedicated folder
```

Do not create folders just to fill a hierarchy. A package should exist
when it has a defined responsibility and code. `testing` fixtures
intended only for maintainers must not accidentally become mandatory
consumer runtime code.

### Dependency direction

``` text
api -> core public contracts
orchestration -> core + strategy/workload abstractions
strategy -> SPI/contracts
algorithms -> domain contracts + Java standard library
execution/runtime -> algorithms + lifecycle contracts
metrics/diagnostics -> observation contracts
integrations -> public API/SPI + optional framework dependencies
testing -> test dependencies only
benchmarks -> public API + benchmark tooling
```

Avoid circular dependencies and prevent internal implementation classes
from leaking into consumer imports.

## 11. Time management contract

Use `java.time.Duration` for durations and `Instant` for externally
meaningful timestamps. Use `System.nanoTime()` or another monotonic
source for elapsed-time and timeout calculations.

  -----------------------------------------------------------------------
  Time concept            Meaning                 Contract requirement
  ----------------------- ----------------------- -----------------------
  Queue wait              Submission to start     Measure separately from
                                                  execution

  Execution timeout       Maximum permitted       Define cancellation and
                          execution duration      result behaviour

  Deadline                Latest acceptable       Do not confuse with a
                          completion point        delay

  Retry delay             Pause between eligible  Apply bounded backoff
                          attempts                and optional jitter

  Rate-limit wait         Time waiting for        Non-blocking by
                          permits                 default; bounded wait
                                                  only via explicit API

  TTL                     Cache value validity    Define write-based or
                          duration                access-based expiry

  Shutdown grace          Time for in-flight work Define what happens
                          to finish               after it expires

  Circuit open duration   Minimum time before     Define half-open
                          recovery probes         transition and probe
                                                  limits
  -----------------------------------------------------------------------

Rules: - Do not use wall-clock timestamps as the only basis for
elapsed-time calculations. - Define zero-timeout semantics; reject
negative durations. - Do not call `Thread.sleep()` inside an API that
appears non-blocking. - Avoid one timer thread per request; cancel
scheduled work and clean up handles. - Use bounded queues and define
overload/backpressure behaviour. - JVM/OS scheduling is not a real-time
guarantee; never promise exact millisecond execution. - Make time
sources injectable for deterministic TTL, retry and circuit-breaker
tests. - State whether a timeout covers queue wait, execution, or
both. - Interrupting a task is a cancellation request, not proof that
arbitrary user code stopped.

## 12. Request lifecycle and failure model

1.  **Receive** the domain request and optional correlation ID.
2.  **Validate** required values, ranges, compatible options and
    capabilities.
3.  **Normalize** configuration into an immutable internal
    representation.
4.  **Select** an explicit strategy or documented automatic rule.
5.  **Execute** the calculation or runtime task as required.
6.  **Measure** configured queue, duration and outcome metrics.
7.  **Return** a typed result with status and relevant metadata.
8.  **Clean up** owned resources, timers, callbacks and completed task
    state.

  -----------------------------------------------------------------------
  Failure category        Example                 Recommended behaviour
  ----------------------- ----------------------- -----------------------
  Invalid input           Negative capacity       Specific validation
                                                  failure

  Unsupported combination Strategy cannot satisfy Fail before execution
                          a constraint            with a clear
                                                  explanation

  Expected decision       Rate limit denied;      Typed decision, not
                          circuit open            generic exception

  User operation failed   Callable throws         Preserve cause in
                                                  result or documented
                                                  exception

  Timeout                 Work exceeds bound      Typed timeout;
                                                  cancellation semantics
                                                  documented

  Resource exhaustion     Queue/capacity full     Defined rejection or
                                                  bounded wait; never
                                                  grow unbounded
                                                  accidentally

  Lifecycle error         Submit after shutdown   Predictable lifecycle
                                                  failure

  Internal defect         Broken invariant        Surface failure; never
                                                  fabricate success
  -----------------------------------------------------------------------

Do not catch every `Throwable` and disguise fatal JVM errors as normal
algorithm failures.

## 13. Thread safety and ownership

Every public service must state whether it is thread-safe,
thread-confined, immutable after construction, or not thread-safe.
Services that own threads or scheduled work should implement
`AutoCloseable`; define idempotency of `close()`, handling of in-flight
work and behaviour after shutdown. Avoid hidden global thread pools. Let
consumers provide an executor where useful, but document who owns and
closes it. Bound queues and worker counts, and define rejection policy.
Remove completed task metadata when safe to avoid leaks.

## 14. Why consumers may choose UnirCore

UnirCore should differentiate through consistency and operational
clarity, not unsupported claims that it is faster than every competitor.

  ---------------------------------------------------------------------------------
  Alternative       Potential pain    UnirCore's intended value   Evidence required
                    point                                         
  ----------------- ----------------- --------------------------- -----------------
  Reimplement       Duplicated code   Shared tested               Reference
  algorithms per    and inconsistent  implementations             correctness tests
  project           edge-case                                     
                    handling                                      

  Use a separate    Different         Consistent                  API review and
  library per       configuration and request/result/validation   consumer feedback
  domain            lifecycle styles  conventions                 

  Couple            Strategy changes  Interfaces and replaceable  Demonstrate
  application code  require caller    policies                    strategy swap
  to concrete       changes                                       without rewriting
  algorithms                                                      caller

  Handwritten       Infinite retries  Bounded attempts, backoff,  Deterministic
  retry/timeout     or unsafe         jitter and idempotency      failure tests
  code              repeated side     guidance                    
                    effects                                       

  Unbounded         Accidental memory Explicit capacity and       Stress/resource
  queues/cache      growth            overload contracts          tests

  Opaque automatic  Hard to           Decision explanation for    Decision tests
  choices           understand why a  supported automatic         
                    strategy was      policies                    
                    chosen                                        

  Framework-heavy   Core feature      Small framework-independent Inspect
  dependencies      brings unwanted   core                        dependency tree
                    dependencies                                  and published POM

  Unsupported       Results vary by   JMH benchmarks with         Reproducible
  performance       workload and      published conditions        benchmark results
  claims            hardware                                      
  ---------------------------------------------------------------------------------

The real value proposition is **a coherent systems-algorithm toolkit
with stable APIs, explicit operational contracts, replaceable strategies
and measurable behaviour**. Benchmark named competitors only with
equivalent workloads, versions, configurations and environments.

## 15. Expected consumer experience

-   Add a Maven dependency and follow a short example.
-   Choose a named strategy or documented automatic selection.
-   Know whether a call blocks, waits, retries, rejects or schedules
    work.
-   Get meaningful status, timing and optional diagnostics.
-   Have bounded capacity, validated configuration and no surprise
    background service.
-   Swap strategies without rewriting business logic.
-   Upgrade through SemVer releases, changelog and migration notes.
-   Use the core without a mandatory framework or OS-specific
    assumption.

If normal use requires understanding internal `planner`, `resolver`,
`dispatcher`, `worker` or registry classes, the public abstraction is
not doing enough work.

## 16. Proposed implementation order

1.  API foundations: configuration, requests/results, validation,
    exceptions, lifecycle contracts.
2.  Scheduling as pure calculation with reference test cases.
3.  Bounded execution, task handles, lifecycle and cancellation/timeout
    semantics.
4.  Cache: capacity, eviction, expiry, concurrency contract and stats.
5.  Rate limiting: first algorithm and deterministic time tests.
6.  Retry policy; add circuit breaker only after its concurrent state
    model is specified.
7.  Resource leases and bounded capacity.
8.  Metrics and diagnostics for supported services.
9.  SPI and optional integrations only where needed.
10. JavaDoc, examples, cross-platform CI, dependency checks and release
    readiness.

The folder structure does not itself make a feature complete. Each
advertised feature needs implementation, tests, JavaDoc, lifecycle
behaviour and a consumer example.

## 17. Phase-1 acceptance checklist

-   [ ] Public signatures and naming conventions reviewed before
    implementation.
-   [ ] Every advertised method has tested behaviour.
-   [ ] Inputs, outputs, errors and timing semantics documented.
-   [ ] Thread safety, lifecycle and resource ownership documented.
-   [ ] Queue/cache/resource capacities bounded with tested overload
    behaviour.
-   [ ] Scheduling results match reference cases.
-   [ ] Retry, rate-limit, cache-expiry and circuit transitions have
    deterministic tests.
-   [ ] No unexpected blocking or sleeping in non-blocking APIs.
-   [ ] Core dependency tree remains minimal.
-   [ ] Examples compile against the packaged artifact.
-   [ ] JavaDoc explains contracts, not just method names.
-   [ ] Supported JDK/OS tests pass.
-   [ ] Performance claims are supported by repeatable benchmarks.
-   [ ] Release notes separate implemented features from future plans.

## 18. Decisions to lock before coding

1.  Exact feature set that can realistically be implemented and tested
    in Phase 1.
2.  Whether scheduling is a pure planning API, execution scheduler, or
    both via separate interfaces.
3.  Queue-timeout versus execution-timeout semantics.
4.  Whether task handles expose `CompletionStage`, `Future`, or a custom
    handle.
5.  Whether automatic strategy selection is rule-based only in Phase 1.
6.  Minimum Java version and JDK matrix.
7.  Thread-safety and background-resource ownership per service.
8.  Initial algorithms for cache, rate limiting and resilience.
9.  Metrics defaults and overhead.
10. Public SPI surface and compatibility policy.

**Recommendation:** Freeze public contracts before implementing
internals. The folder hierarchy organizes the code; the real product is
the behaviour consumers can rely on through the public API.

## 19. References

-   Java `Duration`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html
-   Java `System.nanoTime()`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#nanoTime()
-   Java `AutoCloseable`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/AutoCloseable.html
-   Maven dependency mechanism:
    https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
-   Semantic Versioning: https://semver.org/
-   JMH: https://github.com/openjdk/jmh
