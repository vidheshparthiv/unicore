# UnirCore --- Phase 5

## Testing Standards and Implementation Roadmap

**Document status:** Design proposal\
**Project:** UnirCore --- Java systems and operating-algorithm library\
**Scope:** Combines Topic 5 (testing and performance standards) and
Topic 6 (scope and implementation roadmap).

------------------------------------------------------------------------

## 1. Purpose

This document defines how UnirCore algorithms and services will be
verified, benchmarked, prioritized, and accepted for release. It also
establishes a practical implementation roadmap that protects API quality
and avoids shipping too many unvalidated features.

UnirCore is intended to be a reusable Java library consumed inside
applications. The release standard must therefore cover more than
whether an example runs: public contracts, edge cases, concurrency,
lifecycle behavior, resource limits, performance claims, packaging, and
compatibility all matter.

The plan below is a proposal. An item is not considered implemented
merely because it appears in this roadmap.

## 2. Testing principles

1.  **Test contracts, not implementation details.** Assert documented
    behavior and invariants rather than private methods or internal data
    structures.
2.  **Use deterministic tests wherever possible.** Prefer controlled
    clocks, latches, barriers, and injected dependencies over arbitrary
    sleeps.
3.  **Test failure paths as carefully as success paths.** Include
    invalid inputs, rejected work, timeouts, cancellation, exceptions,
    and cleanup.
4.  **Make concurrency guarantees testable.** Do not claim thread safety
    without tests that exercise concurrent access and state transitions.
5.  **Separate correctness from performance.** Benchmarks measure cost;
    they do not prove correctness.
6.  **Avoid flaky tests.** Tests should not depend on external services,
    the network, real cloud accounts, or machine-specific timing.
7.  **Test the published artifact.** Verify that consumers can resolve
    and use the packaged library with its documented Java baseline.
8.  **Keep test and benchmark dependencies out of runtime.** JUnit and
    JMH belong in test/benchmark configurations, not the core consumer
    dependency graph.

## 3. Testing layers

### 3.1 Unit tests

Purpose: verify one algorithm or component in isolation.

Cover: - Valid and invalid inputs. - Empty, minimal, typical, and
boundary workloads. - Output invariants and ordering. - Deterministic
tie-breaking. - Correct exception or typed-outcome behavior. -
Resource-limit enforcement. - Cleanup after success and failure. -
Configuration defaults and explicit overrides.

Unit tests should be fast, repeatable, and independent of network
access.

### 3.2 Contract tests

A common contract test suite should be reusable across implementations
of the same interface.

Examples: - Every cache implementation obeys the agreed capacity,
invalidation, and result semantics. - Every rate limiter follows the
permit-accounting and timeout contract. - Every scheduler respects its
selection policy and tie-breaking rules. - Every custom strategy rejects
incompatible inputs clearly. - Every execution service follows the
documented lifecycle and shutdown rules.

Contract tests reduce the risk that adding an implementation creates
subtly different public behavior.

### 3.3 Integration tests

Purpose: verify cooperating components, including public API to
orchestration to algorithm implementation.

Test: - Builder/configuration to service construction. - Strategy
selection to algorithm invocation. - Execution service and queue
interactions. - Retry and circuit-breaker composition. - Metrics and
diagnostics attached to real operations. - Service close/shutdown and
resource cleanup. - SPI registration if an SPI is shipped.

Use in-process test fixtures. Core integration tests must not require
Docker, a database, a cloud account, or a hosted service unless a
separately maintained optional integration explicitly requires it.

### 3.4 Concurrency tests

Concurrency tests are required whenever an API promises concurrent use.

Cover: - Simultaneous reads/writes and state inspection. - Race
conditions around cache eviction and expiry. - Concurrent rate-limit
acquisition. - Circuit-breaker transitions and half-open permit
limits. - Concurrent node updates and load-balancer selections. - Task
submission, cancellation, and shutdown races. - Exactly-once or
idempotent cleanup where promised.

Use `CountDownLatch`, `CyclicBarrier`, `Phaser`, controlled executors,
and repeated stress loops where appropriate. Avoid asserting precise
scheduling order unless the contract guarantees it.

A stress test can reveal defects but does not prove the absence of all
concurrency defects. State guarantees conservatively.

### 3.5 Stress and resource-bound tests

Stress tests examine behavior under large or sustained workloads.

Examples: - Large ready queues and repeated scheduling. - Cache capacity
under continuous insertions and invalidations. - High-volume rate-limit
attempts. - Retry storms with bounded attempt counts. - Queue saturation
and task rejection. - Large node lists and repeated membership
updates. - Metrics/diagnostic retention limits under sustained activity.

Assertions should focus on correctness, bounded memory/state,
responsiveness within broad safe limits, and absence of deadlock or
unbounded growth. Do not use fragile machine-specific latency thresholds
as ordinary unit-test assertions.

### 3.6 Regression tests

Every confirmed defect should produce a regression test that fails
before the fix and passes after it.

Regression coverage should include: - The original triggering input or
interleaving. - A minimal reproduction where possible. - Relevant
neighboring boundaries. - Publicly visible behavior, not merely the
private line that caused the issue.

### 3.7 Compatibility and packaging tests

Before a release: - Build from a clean checkout. - Run tests using the
declared supported JDK baseline. - Compile representative consumer
examples against the packaged JAR. - Verify Maven coordinates, POM
metadata, JavaDoc, source JAR, and license information. - Confirm that
test-only and benchmark-only libraries are not runtime dependencies. -
Test that the README installation snippet resolves the intended
version. - Check public API changes against the previous release where
feasible.

If Java 17 remains the chosen baseline, CI must compile and test against
Java 17. A newer developer JDK does not replace verification of the
declared minimum runtime.

## 4. Family-specific test matrix

  -----------------------------------------------------------------------
  Family                              Required checks
  ----------------------------------- -----------------------------------
  Scheduling                          Selection order, tie-breaking,
                                      empty workload, invalid burst
                                      values, quantum validation,
                                      preemption semantics if supported,
                                      calculation invariants

  Cache                               Capacity, eviction order,
                                      replacement, invalidation,
                                      expiration boundary, concurrent
                                      access, statistics semantics

  Rate limiting                       Permit accounting, refill/window
                                      boundaries, immediate denial, wait
                                      timeout, interruption, concurrent
                                      acquisition

  Retry                               Initial-attempt definition, maximum
                                      attempts, retry predicate, delay
                                      bounds, deadline, cancellation,
                                      original-cause preservation

  Circuit breaker                     Closed/open/half-open transitions,
                                      threshold boundaries, trial-call
                                      limit, concurrent transitions,
                                      reset behavior

  Load balancing                      Eligibility filtering, valid
                                      weights, node-list updates,
                                      tie-breaking, no-eligible-node
                                      outcome, distribution properties

  Execution                           Queue-full behavior, task states,
                                      submission/cancellation race,
                                      shutdown modes, timeout, cleanup
                                      after failure

  Resource management                 Acquisition timeout, lease
                                      ownership, release semantics,
                                      capacity limits, cleanup on every
                                      exit path

  Metrics/diagnostics                 Correct counters, bounded
                                      retention, safe concurrent
                                      snapshots, reset semantics, no
                                      impact on core correctness
  -----------------------------------------------------------------------

## 5. Time, randomness, and testability

Time-based and randomized behavior should be controllable in tests.

-   Use an injectable time source for TTL, refill, retry delays, and
    circuit-breaker timing where it improves determinism.
-   Use monotonic time for elapsed durations; wall-clock timestamps
    should not be used to measure elapsed time.
-   Keep production defaults simple; test hooks must not become
    unnecessary complexity in the public API.
-   If jitter or random selection is supported, allow deterministic
    seeds or an injected random source where useful.
-   Test boundary instants explicitly: just before expiry, at expiry,
    and just after expiry.
-   Do not wait through real retry backoffs or circuit-open periods in
    ordinary unit tests when virtual or controlled time can test the
    same rule.

## 6. Performance benchmark policy

### 6.1 Use JMH for JVM microbenchmarks

Use Java Microbenchmark Harness (JMH) for algorithm-level performance
measurements. Avoid ad-hoc `System.nanoTime()` loops as the basis for
published benchmark claims.

Benchmarks should account for: - Warm-up and measurement iterations. -
Multiple forks where appropriate. - JVM version and flags. - Heap
settings and garbage collection. - Input size and workload
distribution. - Thread count and concurrency level. - Setup cost versus
operation cost. - Allocation rate and garbage-collection effects when
relevant.

Do not include test-fixture creation in the measured operation unless
setup cost is explicitly what the benchmark is intended to measure.

### 6.2 Benchmark questions, not just algorithms

Useful benchmark comparisons include: - Scheduler selection cost as
ready-queue size grows. - Cache hit/miss cost and eviction overhead at
different capacities. - Rate-limit acquisition under single-threaded and
contended workloads. - Retry/circuit-breaker overhead when operations
succeed, fail, or are rejected. - Load-balancer selection cost as node
count grows. - Metrics enabled versus disabled, if observability
overhead is material.

Compare implementations only when they perform equivalent work and obey
equivalent correctness requirements.

### 6.3 Performance claims

Do not publish claims such as "fastest," "zero overhead," "constant-time
in all cases," or "handles any workload" without strong evidence and
carefully stated conditions.

A benchmark report should include: - The exact release/commit. -
Hardware and operating system. - JDK and JVM options. - Workload, data
size, thread count, and configuration. - Warm-up and measurement
method. - Results and known limitations. - Whether the numbers are
throughput, average time, sample-time percentiles, allocation, or
another metric.

Benchmarks are evidence for a particular environment and workload, not
universal guarantees.

### 6.4 Performance regression policy

Initially, keep benchmark results informational rather than making every
benchmark a hard CI gate. Machine variability can produce noisy
failures.

Introduce a performance gate only when: - The benchmark is stable and
representative. - The execution environment is controlled. - The
regression threshold is justified. - Repeated measurements can
distinguish meaningful regressions from noise.

Correctness and bounded resource behavior should be release gates from
the beginning.

## 7. CI pipeline and quality gates

Recommended GitHub Actions pipeline:

1.  Checkout source.
2.  Set up the declared minimum JDK.
3.  Run formatting/style checks if adopted.
4.  Compile and run unit tests.
5.  Run contract and integration tests.
6.  Run relevant concurrency tests.
7.  Package the JAR.
8.  Verify JavaDoc and source artifacts.
9.  Check dependency scope and dependency vulnerabilities using a
    selected, maintained tool.
10. On release tags, run release-specific packaging and publication
    checks.

A newer JDK can be added to a compatibility matrix after the minimum-JDK
pipeline is stable. Do not make the initial matrix unnecessarily large.

### Required merge gates

-   Build succeeds.
-   Tests pass.
-   No known failing regression test is ignored without a documented
    reason.
-   Public API and JavaDoc remain consistent.
-   Runtime dependencies stay within the approved dependency policy.
-   No secrets or credentials are committed.
-   Changes to behavior, defaults, and compatibility receive review.

Publishing to Maven Central should be a controlled release workflow, not
an automatic side effect of every branch build.

## 8. Phase-1 scope and priorities

### 8.1 Priority definitions

-   **P0 --- Foundation:** required for a reliable first release.
-   **P1 --- Initial algorithms:** first set of useful algorithm
    implementations.
-   **P2 --- Validated extension:** add only after the core contracts
    and tests are stable.
-   **Deferred:** outside the first release unless explicitly
    re-approved.

### 8.2 Proposed scope

  -----------------------------------------------------------------------
  Area                    Priority                Phase-1 recommendation
  ----------------------- ----------------------- -----------------------
  API contracts,          P0                      Build first; shared
  validation, errors                              conventions across all
                                                  services

  Scheduling models       P1                      FCFS, SJF, priority,
                                                  and Round Robin; add
                                                  SRTF only with clear
                                                  preemption/simulation
                                                  semantics

  Cache                   P1                      LRU and FIFO with
                                                  bounded capacity; TTL
                                                  if expiration semantics
                                                  and timing tests are
                                                  ready

  Rate limiting           P1                      Start with one
                                                  thoroughly tested
                                                  in-process policy,
                                                  preferably token bucket

  Retry                   P1                      Bounded attempts, clear
                                                  retry predicate,
                                                  deadline/cancellation
                                                  behavior

  Circuit breaker         P1/P2                   Add after state
                                                  transitions and retry
                                                  composition are fully
                                                  specified and tested

  Load balancing          P1                      Round Robin; Weighted
                                                  Round Robin if weights
                                                  and updates are well
                                                  defined

  Execution/lifecycle     P0/P1                   Bounded queues, task
                                                  lifecycle,
                                                  cancellation, shutdown,
                                                  cleanup

  Resource manager        P2                      Add only for a concrete
                                                  resource-lease use case
                                                  with clear ownership

  Metrics/diagnostics     P0/P1                   Minimal useful counters
                                                  and bounded snapshots;
                                                  no heavy observability
                                                  stack

  Automatic selection     P2                      Rule-based and
                                                  explainable; only for
                                                  families with validated
                                                  alternatives

  Custom SPI              P2                      Add when there is a
                                                  concrete extension use
                                                  case

  LFU cache               Deferred/P2             Validate frequency
                                                  tracking, tie-breaking,
                                                  and overhead first

  Least-Connections       Deferred                Requires reliable
  balancing                                       connection/load signals

  Distributed             Deferred                Separate design and
  coordination                                    integration
                                                  requirements

  Full virtual            Deferred                Separate product scope
  filesystem/storage                              
  engine                                          

  Framework integrations  Deferred                Optional artifacts only
                                                  after core stability
  -----------------------------------------------------------------------

This table is a prioritization proposal. The final set should be
adjusted based on implementation effort, correctness results, and the
library's first target users---not the number of features that can be
listed.

## 9. Implementation roadmap

A realistic schedule depends on available time and how much
implementation/testing is done each day. The sequence below is a
milestone plan rather than a guarantee of delivery dates.

### Milestone 1 --- Repository and API foundation

Deliverables: - Standard Maven layout and package naming. - Java
baseline and compiler configuration. - Public API conventions, immutable
request/result models, and validation approach. - Exception and
typed-outcome policy. - CI build, unit-test setup, license, contribution
rules, and versioning convention.

Exit criteria: - Clean build from checkout. - A small sample consumer
compiles against the packaged artifact. - No runtime dependency has been
added without justification.

### Milestone 2 --- First algorithm family

Deliverables: - Scheduling model with documented semantics. - Input
validation and deterministic tie-breaking. - Family-specific unit and
contract tests. - JavaDoc and a runnable example.

Exit criteria: - Every supported policy passes its correctness tests. -
Simulation and live execution are not confused in the API. - Complexity
and limitations are documented.

### Milestone 3 --- Cache and time-based behavior

Deliverables: - Bounded cache with chosen eviction policies. -
Invalidation and statistics semantics. - TTL only if its clock/expiry
model is settled. - Concurrency tests and relevant benchmarks.

Exit criteria: - Capacity and eviction rules are proven by tests. -
Expiration and concurrent access behavior are documented. - Memory use
remains bounded by the configured policy.

### Milestone 4 --- Rate limiting and resilience

Deliverables: - One in-process rate limiter. - Bounded retry policy. -
Circuit breaker only after state and composition rules are settled. -
Controlled-clock tests, failure-path tests, and concurrency tests.

Exit criteria: - No unbounded retry loop. - Timeout and cancellation
rules are tested. - Expected rejections are distinct from unexpected
failures.

### Milestone 5 --- Load balancing and execution lifecycle

Deliverables: - Round Robin and, if ready, Weighted Round Robin. -
Bounded execution queue and clear rejection behavior. - Task lifecycle,
cancellation, and shutdown semantics. - Resource cleanup and lifecycle
integration tests.

Exit criteria: - No undocumented fallback when no eligible node or
capacity is available. - Shutdown and cancellation behavior matches the
public contract. - Concurrent updates and task lifecycle races are
tested.

### Milestone 6 --- Release readiness

Deliverables: - API review and compatibility check. - Minimal metrics
and diagnostics. - Stable README, JavaDoc, installation snippet, and
examples. - JAR, sources, JavaDoc, POM metadata, license, and release
notes. - Reproducible benchmark report for any published performance
claims. - Maven Central release workflow and tag/version verification.

Exit criteria: - All required tests and packaging checks pass. -
Examples compile against the candidate release. - No feature is
advertised as complete unless its acceptance criteria are met.

## 10. Definition of done for each feature

A feature is not complete merely because its main algorithm works for
one happy-path input. "Done" requires:

-   [ ] Public purpose and intended use are documented.
-   [ ] Input and output contracts are stable enough for release.
-   [ ] Validation and failure behavior are specified.
-   [ ] Boundary and regression tests pass.
-   [ ] Concurrency behavior is tested if promised.
-   [ ] Resource usage is bounded or explicitly documented.
-   [ ] Complexity and limitations are stated.
-   [ ] JavaDoc and at least one useful example exist.
-   [ ] Metrics/diagnostics, if exposed, have clear semantics.
-   [ ] Runtime dependencies remain within policy.
-   [ ] Public API changes have been reviewed for compatibility.

## 11. Release candidate checklist

Before creating a stable release candidate:

-   [ ] Clean checkout builds with the minimum supported JDK.
-   [ ] Unit, contract, integration, concurrency, and regression tests
    pass.
-   [ ] No flaky test is silently ignored.
-   [ ] No unresolved high-severity correctness or resource-bound issue
    remains.
-   [ ] Public API, JavaDoc, examples, and README agree.
-   [ ] JAR, sources, JavaDoc, POM, and license artifacts are verified.
-   [ ] Test and benchmark libraries are not runtime dependencies.
-   [ ] Dependency and secret checks pass.
-   [ ] Benchmark claims are reproducible and carefully scoped.
-   [ ] Version number, release notes, and Git tag match.
-   [ ] Maven Central publication is performed through the intended
    release workflow.

## 12. Risk register

  -----------------------------------------------------------------------
  Risk                                Mitigation
  ----------------------------------- -----------------------------------
  Too many algorithms delay a         Start with a small, tested subset
  reliable release                    and expand based on user value

  Flaky time/concurrency tests        Inject clocks and synchronization
                                      controls; avoid arbitrary sleeps

  Misleading benchmark claims         Use JMH and publish
                                      environment/workload details

  API changes break consumers         Keep APIs small, document
                                      semantics, and review compatibility

  Unbounded memory or queues          Establish bounded defaults and test
                                      saturation behavior

  Retry amplifies failures            Bound attempts, respect deadlines,
                                      and document idempotency concerns

  Automatic selection hides behavior  Keep it opt-in, deterministic,
                                      explainable, and compatible with
                                      hard constraints

  Optional integrations inflate core  Keep integrations separate from the
  dependencies                        core artifact

  Release works locally but not for   Test the packaged artifact and the
  consumers                           documented installation
                                      instructions
  -----------------------------------------------------------------------

## 13. Decisions to finalize

1.  Confirm the exact Java baseline and test matrix.
2.  Finalize which scheduling algorithms are included in the first
    release.
3.  Select one initial rate-limiting algorithm.
4.  Decide whether TTL is included in the first cache release.
5.  Decide whether the circuit breaker is P1 or follows a separate
    validation milestone.
6.  Confirm whether resource management is needed for the first consumer
    use cases.
7.  Decide the minimum metrics and diagnostic surface.
8.  Define the release gates and the Maven Central publication process.

**Recommendation:** optimize for a small, dependable first release. Add
an algorithm only when its contract, implementation, tests,
documentation, and consumer use case are all clear.

------------------------------------------------------------------------

## Final rule

**A release is ready when its behavior is predictable, its limits are
documented, its tests are repeatable, and its published artifacts work
for consumers.** Feature count and benchmark numbers are not substitutes
for those guarantees.
