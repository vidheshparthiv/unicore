# UnirCore --- Phase 4

## Algorithm Contracts and Strategy Selection

**Document status:** Design proposal\
**Project:** UnirCore --- Java systems and operating-algorithm library\
**Purpose:** Define how algorithms are specified, selected, executed,
and extended without making unsupported claims about optimality.

------------------------------------------------------------------------

## 1. Objective

UnirCore should expose algorithms through stable, purpose-oriented Java
APIs. Consumers should be able to choose an algorithm explicitly, use a
documented deterministic selection policy, or provide a custom strategy
where the extension point is supported.

This document defines:

-   The minimum contract every algorithm must satisfy.
-   The initial algorithm families and the boundaries of Phase 1.
-   Input and output expectations, validation, complexity documentation,
    and concurrency guarantees.
-   Explicit, rule-based automatic, and custom strategy selection.
-   How unsupported capabilities and invalid configurations are
    reported.
-   The tests required before an algorithm can be considered
    release-ready.

The algorithms and APIs described here are **proposed design targets**,
not claims that they have already been implemented.

## 2. Core design principles

1.  **Explicit beats magical.** A consumer must be able to identify
    which algorithm is running.
2.  **No false optimality claims.** Automatic selection follows
    documented rules; it does not promise a globally optimal result.
3.  **Contracts before implementations.** Each algorithm has defined
    inputs, outputs, preconditions, postconditions, failure modes, and
    concurrency behavior.
4.  **Validate at the boundary.** Reject invalid requests before they
    reach algorithm internals.
5.  **Bound resource use.** Queues, retries, caches, retained samples,
    and other stateful resources need configurable limits and safe
    defaults.
6.  **Keep core portable.** Core algorithms should use the JDK and avoid
    mandatory cloud, database, or framework dependencies.
7.  **Make trade-offs visible.** Document complexity, fairness,
    accuracy, memory use, and limitations.
8.  **Keep policies replaceable.** Separate algorithm mechanics from
    selection policy and runtime orchestration.

## 3. Standard algorithm contract

Each public algorithm implementation should document and, where
applicable, expose the following.

  -----------------------------------------------------------------------
  Contract element                    Required definition
  ----------------------------------- -----------------------------------
  Name and purpose                    The problem it solves and when to
                                      use it

  Input                               Required fields, optional fields,
                                      defaults, and valid ranges

  Output                              Result type, ordering guarantees,
                                      and meaning of each field

  Preconditions                       Conditions that must be true before
                                      execution

  Postconditions                      Guarantees after successful
                                      execution

  Validation                          How malformed or incompatible
                                      inputs are rejected

  Complexity                          Time and space complexity, with
                                      assumptions stated

  Determinism                         Whether identical inputs produce
                                      equivalent results

  Thread safety                       Whether concurrent calls are
                                      supported and how shared state is
                                      handled

  Resource limits                     Bounds on queue size, memory,
                                      retries, or retained history

  Failure behavior                    Exceptions versus expected domain
                                      outcomes

  Observability                       Which metrics or diagnostic details
                                      are available

  Version compatibility               Whether the public contract is
                                      stable and what changes require a
                                      major version
  -----------------------------------------------------------------------

A complexity statement must describe the actual implementation, not just
the textbook algorithm. For example, a priority scheduler using a heap
may have different insertion and selection costs from one that scans a
list.

## 4. Common input and output conventions

### 4.1 Request models

-   Prefer immutable, purpose-specific request objects.
-   Validate required values in constructors or a dedicated validation
    boundary.
-   Use `Duration` for timeouts and elapsed-time limits.
-   Use `Instant` for wall-clock timestamps and a monotonic time source
    for elapsed durations.
-   Avoid a single universal request class filled with nullable fields.
-   Give collections clear ordering semantics and defensively copy
    mutable input where necessary.
-   Document whether the library retains references to caller-supplied
    tasks, keys, values, or nodes.

### 4.2 Result models

-   Return typed results with clearly named fields.
-   Distinguish a valid domain outcome from a programming/configuration
    error.
-   Include selected algorithm or strategy identifiers where that helps
    explain behavior.
-   Do not expose internal mutable data structures.
-   Avoid returning `null` to represent ordinary outcomes such as "no
    eligible node" or "rate limit denied"; use explicit result states
    where appropriate.

### 4.3 Error and outcome policy

Use exceptions for invalid requests, incompatible configurations, closed
services, and unexpected execution failures. Use typed outcomes for
normal domain decisions such as:

-   A rate-limit permit was not available.
-   A circuit breaker is open and the operation was not attempted.
-   No eligible node exists for a load-balancing request.
-   A scheduler has no ready task at the time of inspection.

Follow the exception taxonomy and resource-handling rules in Phase 3. Do
not create a second, conflicting error model in individual algorithm
packages.

## 5. Phase-1 algorithm families

The following list is the proposed initial scope. Each item must pass
its contract and test requirements before release.

### 5.1 CPU scheduling models

These are **application-level scheduling models**. They do not replace
an operating system kernel scheduler.

  -----------------------------------------------------------------------
  Algorithm               Contract and behavior   Important constraints
  ----------------------- ----------------------- -----------------------
  First-Come,             Selects the eligible    Simple and predictable;
  First-Served (FCFS)     job that entered the    a long job can delay
                          ready queue first.      short jobs.

  Shortest Job First      Selects the eligible    Requires a usable burst
  (SJF)                   job with the smallest   estimate; estimation
                          declared or estimated   quality affects
                          total burst time.       results.

  Shortest Remaining Time Selects the job with    Preemption and
  First (SRTF)            the least remaining     remaining-time updates
                          estimated processing    must be defined
                          time.                   explicitly.

  Priority scheduling     Selects an eligible job Tie-breaking and
                          according to configured starvation behavior
                          priority ordering.      must be documented;
                                                  aging is a separate
                                                  policy if included.

  Round Robin             Cycles through ready    Quantum must be
                          jobs using a configured positive; this model
                          time quantum.           must define what a time
                                                  slice means when tasks
                                                  are simulated versus
                                                  actually executed.
  -----------------------------------------------------------------------

**Scheduling contract**

A scheduling request should identify the workload, relevant job
attributes, and the selected scheduling policy. A scheduling result
should identify the selected job or ordered schedule, the strategy used,
and any applicable estimates or explanation.

The contract must state whether the algorithm is: - A
**simulation/planning algorithm** that computes a schedule from supplied
workload data; or - A **live execution scheduler** that dispatches real
tasks.

Do not silently combine the two. Phase 1 should begin with a clearly
bounded scheduling model; real task execution belongs to the execution
subsystem and must respect its lifecycle and cancellation contracts.

Validation should cover missing job identifiers, duplicate identifiers
where prohibited, negative burst times, invalid priorities if the
selected policy constrains them, non-positive time quantum, and
inconsistent remaining-time values.

### 5.2 Cache eviction policies

  -----------------------------------------------------------------------
  Policy                  Contract and behavior   Important constraints
  ----------------------- ----------------------- -----------------------
  LRU                     Evicts the least        Reads update recency;
                          recently accessed       concurrent access must
                          eligible entry.         not corrupt ordering.

  FIFO                    Evicts the entry that   Accessing an entry does
                          has been resident       not change its
                          longest.                insertion order.

  TTL expiration          Makes an entry          Define whether expiry
                          ineligible after its    is lazy, scheduled, or
                          configured lifetime.    both; use monotonic
                                                  time for elapsed
                                                  lifetime.

  LFU (candidate)         Evicts an entry with    Include only if counter
                          the lowest access       updates, ties, aging,
                          frequency.              and overhead are tested
                                                  and documented.
  -----------------------------------------------------------------------

**Cache contract**

A cache configuration must define capacity or another bounded resource
policy. Define behavior for zero/invalid capacity, null keys or values
(if supported), expiration, replacement, and concurrent access. Do not
claim strict O(1) operations unless the chosen data structures and
synchronization strategy support that claim.

Cache statistics should be clearly defined: for example, hits, misses,
evictions, expirations, and current entry count. State whether counters
are approximate under concurrency.

### 5.3 Rate limiting

Initial candidates:

-   **Token bucket:** permits bursts up to bucket capacity while
    enforcing a long-term refill rate.
-   **Fixed window:** counts permits within fixed time windows; simpler
    but may allow bursts around window boundaries.
-   **Sliding window:** smooths boundary effects, with implementation
    and memory trade-offs that must be documented.

Choose the initial implementation set after implementation feasibility
and correctness tests. Do not ship all variants merely to increase the
feature count.

**Rate-limit contract**

-   Configure the permitted rate, capacity/window, and time source
    explicitly.
-   Reject non-positive or otherwise invalid permit requests according
    to a documented policy.
-   Distinguish immediate `tryAcquire` from a potentially waiting
    `acquire`.
-   A waiting call must honor its timeout and interruption/cancellation
    policy.
-   Use monotonic time for refill and elapsed calculations.
-   Define whether the limiter is thread-safe and whether fairness is
    guaranteed.
-   State that an in-process limiter is not a distributed,
    cross-instance quota system.

A normal denial should be represented as a result, not as an exception.

### 5.4 Retry and circuit breaker

**Retry policy candidates** - Maximum attempt count, with a precise
definition of whether the initial call counts as an attempt. - Bounded
delay and configurable backoff. - Optional jitter where appropriate. -
Retry predicates that identify retryable failures. - Overall deadline
and interruption/cancellation behavior.

Retries must not be enabled blindly for non-idempotent operations.
Unbounded retry loops are prohibited. The retry layer should preserve
the original failure context and avoid hiding the final cause.

**Circuit breaker states** - `CLOSED`: calls are allowed and outcomes
are recorded. - `OPEN`: calls are rejected without invoking the
protected operation. - `HALF_OPEN`: a bounded number of trial calls are
allowed to assess recovery.

The contract must define thresholds, open duration, permitted half-open
calls, concurrent state transitions, and reset behavior. State
transitions must be tested under concurrency. Circuit-breaker rejection
is an expected typed outcome; operation failure is distinct from breaker
rejection.

The composition order between retry and circuit breaker must be
documented. Retrying inside a breaker and retrying outside a breaker
produce different metrics and traffic patterns; UnirCore must not choose
the order implicitly without a documented policy.

### 5.5 Load balancing

Initial candidates:

-   **Round Robin:** cycles through eligible nodes.
-   **Weighted Round Robin:** allocates selections according to
    configured node weights.
-   **Least Connections (candidate):** selects the eligible node with
    the fewest active connections, only if trustworthy load/connection
    signals are available.

**Load-balancer contract**

-   Define node identity, eligibility, weight rules, and behavior when
    no node is eligible.
-   Define how node-list updates are applied while requests are being
    selected.
-   Make tie-breaking deterministic where practical.
-   Validate duplicate identities, invalid weights, and empty node lists
    according to policy.
-   Do not infer node health from a stale or absent signal.
-   Do not claim network discovery, cross-process coordination, or
    production service-mesh behavior from an in-process selection
    algorithm.

Round Robin and Weighted Round Robin are reasonable initial candidates.
Least Connections should remain deferred unless the library has a
reliable, well-defined source of connection counts.

### 5.6 Execution and resource management boundaries

The execution subsystem may expose task submission, batch submission,
cancellation, inspection, and controlled shutdown. Those are lifecycle
operations rather than standalone scheduling algorithms.

The contract must define: - Whether queues are bounded and what happens
when full. - Whether submission blocks, rejects, or returns an explicit
rejection result. - Task state transitions and visibility. - Best-effort
cooperative cancellation behavior. - Shutdown modes and grace-period
handling. - Cleanup of acquired resources after success, failure,
timeout, or cancellation.

Resource leases should make ownership and release semantics explicit. A
lease must not be silently released twice unless the contract explicitly
makes release idempotent. Do not imply that an application-level
resource manager controls physical operating-system resources.

### 5.7 Features not automatically included in Phase 1

Keep these out of the initial release unless separately approved and
designed:

-   Production distributed locks and leader election.
-   A complete virtual filesystem or storage engine.
-   Kernel-level scheduling, physical-memory management, or disk
    control.
-   Claims of globally optimal automatic algorithm selection.
-   Mandatory Spring Boot or other framework dependencies.
-   Least-Connections load balancing without reliable measurements.
-   LFU cache before its correctness and overhead are validated.
-   Distributed rate limiting across application instances.

Deferring these features protects API stability and prevents the initial
release from claiming capabilities it cannot reliably deliver.

## 6. Strategy selection model

UnirCore should support three distinct selection modes. They must remain
visibly different in the API and documentation.

### 6.1 Explicit selection

The consumer names the algorithm or policy directly.

Example concept:

``` java
Scheduler scheduler = Scheduler.builder()
    .strategy(SchedulingStrategy.ROUND_ROBIN)
    .build();
```

This is illustrative API shape, not a committed signature. Explicit
selection is appropriate when the consumer understands the workload and
needs predictable behavior.

### 6.2 Rule-based automatic selection

The consumer supplies a validated workload profile and asks UnirCore to
choose from a documented set of supported strategies.

The selector should: 1. Validate the workload profile. 2. Filter out
strategies that cannot satisfy the request's constraints. 3. Apply
deterministic, documented rules to the remaining candidates. 4. Return
the chosen strategy and a concise explanation. 5. Report that no
compatible strategy exists if all candidates are filtered out.

Example rule policy for scheduling might consider whether preemption is
required, whether burst-time estimates are available, whether fairness
is a requirement, and whether the workload is a simulation or live
dispatch. The rules must be specific enough to test and explain.

Automatic selection **does not mean the library has discovered the
globally best algorithm**. It means the library applied the published
policy to the information supplied.

### 6.3 Custom strategy via SPI

An SPI (Service Provider Interface) is an extension point that allows
consumers to supply their own implementation without modifying UnirCore
internals.

A custom strategy should: - Implement a small, stable public
interface. - Declare the capability and configuration requirements it
supports. - Validate its configuration before use. - Have a unique,
stable identifier. - Document its concurrency guarantees and resource
behavior. - Fail clearly when requested capabilities are unsupported.

Use `ServiceLoader` only if runtime discovery materially improves the
extension experience. Otherwise, explicit builder registration is
simpler and easier to debug. Do not introduce plugin discovery for every
algorithm family by default.

### 6.4 Selection precedence

Recommended precedence:

1.  An explicit consumer-selected strategy, if valid and compatible.
2.  A registered custom strategy, if the consumer explicitly selects it
    or a documented policy selects it.
3.  The built-in rule-based selector, only when automatic selection was
    requested.
4.  A typed unsupported/incompatible result or a documented exception
    when no valid candidate exists.

Never silently replace an explicitly requested strategy with a different
one. If fallback is supported for a specific operation, it must be
opt-in and observable.

## 7. Capability and compatibility model

Selection should be based on declared requirements and capabilities, not
class-name guessing.

Possible capability dimensions include: - Supports preemption. -
Requires burst-time estimates. - Supports concurrent calls. - Supports
bounded waiting. - Supports weighted nodes. - Supports expiration. -
Supports cancellation-aware execution. - Requires external state or
coordination. - Provides deterministic output for identical inputs.

Avoid a generic, untyped map of arbitrary capability strings as the main
public API. Prefer typed capability metadata for stable features, with a
narrow extension mechanism for future additions.

A strategy is compatible only when it satisfies the request's mandatory
constraints. Preference hints can influence selection, but must not
override hard constraints.

## 8. Complexity and performance documentation

Each algorithm should have a complexity section in its JavaDoc or
reference documentation.

For every complexity statement: - State the relevant input size, such as
number of jobs, entries, or nodes. - Identify the operation being
measured: insert, select, update, evict, or full run. - State
assumptions about data structures and access patterns. - Distinguish
expected, amortized, and worst-case complexity. - Note synchronization
or contention overhead where it materially changes performance. - Avoid
unsupported throughput or latency numbers in the README.

Examples of useful statements: - "Selection scans all ready jobs; cost
grows linearly with ready-queue size." - "Heap insertion and removal are
logarithmic in the number of queued jobs; construction cost depends on
how the initial queue is built." - "The result is deterministic for the
same normalized input and tie-breaking configuration."

Benchmark claims should be published only after repeatable JMH
measurements, with environment, workload, JVM, and configuration
documented. See Phase 5 for the testing and benchmark standard.

## 9. Thread safety and state ownership

Every public service must state whether it is: - Immutable and safe for
concurrent use. - Stateful and safe for concurrent use. - Stateful and
intended for single-threaded use. - Not thread-safe, requiring caller
synchronization.

Additional rules:

-   Do not share mutable algorithm state between service instances
    unless that behavior is deliberate and documented.
-   Keep state ownership explicit: per operation, per service, or
    shared.
-   Do not rely on callers to serialize access when the API claims
    thread safety.
-   Define how configuration updates interact with in-flight operations.
-   Ensure metrics do not corrupt algorithm correctness.
-   Do not guarantee strict fairness unless the implementation enforces
    and tests it.

Thread safety is a contract, not an assumption based on the use of Java
collections.

## 10. Failure handling and fallback

Follow Phase 3's error taxonomy. In particular:

-   Invalid input: validation failure at the public boundary.
-   Unsupported capability: explicit unsupported-capability outcome or
    exception.
-   Incompatible configuration: clear configuration/compatibility
    failure.
-   Service already closed: service lifecycle failure.
-   Expected policy decision: typed domain result.
-   Unexpected implementation failure: wrapped with useful context while
    preserving the cause.

Fallback rules must be explicit. For example, a load balancer may be
configured to skip ineligible nodes, but it must not invent an eligible
node when none exists. A cache may treat an expired entry as a miss, but
should not return an expired value unless stale reads are a documented
option.

## 11. Required contract tests

Every algorithm implementation must pass relevant tests before being
marked stable.

### 11.1 General tests

-   Valid and invalid boundary inputs.
-   Empty and minimal workloads.
-   Duplicate or conflicting identifiers.
-   Deterministic tie-breaking where promised.
-   Output invariants and ordering.
-   Large but permitted inputs.
-   Resource-limit enforcement.
-   Correct failure type and preservation of root cause.
-   No accidental mutable-state leakage.
-   Concurrent access tests when thread safety is promised.

### 11.2 Family-specific tests

  -----------------------------------------------------------------------
  Family                              Essential correctness checks
  ----------------------------------- -----------------------------------
  Scheduling                          Correct selection order,
                                      tie-breaking, turnaround/wait
                                      calculations where provided,
                                      preemption behavior where
                                      supported, invalid burst/quantum
                                      rejection

  Cache                               Capacity enforcement, correct
                                      eviction order, expiration
                                      boundary, replacement, concurrent
                                      reads/writes, statistics
                                      consistency

  Rate limiter                        Permit accounting, refill/window
                                      boundaries, timeout behavior,
                                      concurrent acquisition,
                                      monotonic-clock behavior

  Retry                               Attempt-count semantics, retryable
                                      versus non-retryable failures,
                                      bounded delay, deadline and
                                      cancellation handling

  Circuit breaker                     State transitions, threshold
                                      boundaries, half-open permit
                                      limits, concurrent transitions,
                                      reset behavior

  Load balancing                      Eligibility filtering, weight
                                      validation, distribution behavior,
                                      node updates, empty eligible set

  Execution/resources                 Queue-full policy, lifecycle
                                      transitions, cancellation
                                      semantics, shutdown grace period,
                                      release and cleanup on all exit
                                      paths
  -----------------------------------------------------------------------

Use deterministic clocks and controllable test hooks where practical.
Tests must not depend on arbitrary sleeps when a deterministic clock or
synchronization primitive can verify the behavior.

## 12. API evolution and compatibility

-   Keep public interfaces small and purpose-specific.
-   Avoid exposing implementation classes in public method signatures.
-   Do not make internal tuning fields part of the stable API without a
    clear use case.
-   Treat changes to semantics, defaults, exception behavior, ordering
    guarantees, or thread-safety promises as potential compatibility
    changes---not only method-signature changes.
-   Deprecate public API before removal where practical.
-   Use semantic versioning: incompatible public API or contract changes
    require a major version; compatible functionality additions
    generally use a minor version; compatible fixes use a patch version.
-   Keep examples aligned with the released API, not future proposed
    APIs.

## 13. Proposed implementation order

A risk-conscious implementation order is:

1.  Define shared request/result conventions and validation contracts.
2.  Implement and test scheduling models as bounded, well-specified
    algorithms.
3.  Implement cache policies with clear concurrency and capacity
    behavior.
4.  Implement rate limiting with deterministic time-based tests.
5.  Implement retry and circuit breaker only after deadline,
    cancellation, and composition semantics are fixed.
6.  Implement Round Robin and Weighted Round Robin load balancing.
7.  Add capability metadata and rule-based selection only for families
    with enough validated strategies to make the selector useful.
8.  Add custom SPI where a real consumer extension use case justifies
    it.
9.  Publish complexity notes, JavaDoc, and examples alongside each
    stable algorithm.

Do not build a universal automatic selector before the algorithms it
selects have stable contracts and meaningful compatibility metadata.

## 14. Release acceptance checklist

An algorithm is ready for a stable release only when:

-   [ ] Its purpose, input, output, preconditions, and postconditions
    are documented.
-   [ ] Boundary validation and failure behavior are specified.
-   [ ] Complexity claims match the implementation.
-   [ ] Determinism and tie-breaking are defined where applicable.
-   [ ] Thread safety and state ownership are documented.
-   [ ] Resource limits and cleanup behavior are defined.
-   [ ] Family-specific correctness tests pass.
-   [ ] Concurrency tests pass where concurrency is supported.
-   [ ] Performance claims, if any, are backed by reproducible
    benchmarks.
-   [ ] Strategy selection is explicit, deterministic, and explainable.
-   [ ] Unsupported capabilities fail clearly instead of silently
    degrading.
-   [ ] JavaDoc and examples use the actual released API.
-   [ ] No mandatory framework or cloud dependency has leaked into the
    core artifact.

## 15. Decisions to finalize before implementation

The following decisions should be confirmed during the implementation
roadmap phase:

1.  Whether Phase 1 scheduling is simulation-only or includes live task
    dispatch. **Recommendation:** start with a clearly scoped scheduling
    model and keep actual task execution in the execution subsystem.
2.  Whether the first cache release includes only LRU/FIFO/TTL or also
    LFU. **Recommendation:** defer LFU until its trade-offs are
    validated.
3.  Whether the first rate limiter includes token bucket only or a
    second window-based policy. **Recommendation:** begin with one
    well-tested policy.
4.  Whether load balancing initially includes only Round Robin and
    Weighted Round Robin. **Recommendation:** yes; defer Least
    Connections until reliable load signals exist.
5.  Whether custom SPI registration uses explicit builders or
    `ServiceLoader`. **Recommendation:** begin with explicit
    registration; add discovery only if it materially benefits
    consumers.
6.  Which algorithm families receive automatic strategy selection in the
    first release. **Recommendation:** enable it only where documented
    rules and validated alternatives exist.

------------------------------------------------------------------------

## Final design rule

**Every algorithm must be predictable at the contract level, testable at
the behavior level, and honest about its trade-offs.** UnirCore should
optimize for reliable reuse and clear consumer control---not algorithm
count, hidden fallback, or claims of universal optimality.
