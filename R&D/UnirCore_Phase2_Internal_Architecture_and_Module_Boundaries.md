# UnirCore Phase 2 --- Internal Architecture and Module Boundaries

**Status:** Proposed architecture for review before implementation\
**Scope:** Package responsibilities, dependency direction, request flow,
extension boundaries and architecture rules.

> This document specifies the intended design; it does not claim that
> all packages or features already exist. Create packages when they have
> a real responsibility, not merely to fill a directory tree.

## 1. Architecture objectives

UnirCore should expose a stable public API while allowing algorithms and
runtime implementations to evolve independently. The architecture must
separate consumer contracts from implementation details, avoid circular
dependencies, keep the core framework-independent, make strategy
decisions testable, distinguish pure calculations from stateful runtime
services, and define resource ownership.

## 2. Architecture overview

``` mermaid
flowchart TB
    C[Consumer application] --> A[Public API]
    A --> M[Core contracts and models]
    A --> O[Orchestration when needed]
    O --> W[Workload analysis]
    O --> S[Strategy selection]
    S --> P[SPI contracts]
    O --> G[Algorithm interfaces]
    G --> I[Algorithm implementations]
    I --> R[Runtime services when needed]
    R --> E[Execution engine]
    A -. optional observations .-> T[Metrics]
    O -. decision details .-> D[Diagnostics]
    R -. execution observations .-> T
    X[Optional integrations] --> A
    X --> P
```

This is a conceptual map, not a requirement that every request pass
through every component. A cache lookup should not need a
general-purpose workload planner.

## 3. Responsibility of each package

### `api/` --- Consumer-facing domain APIs

**Owns:** Stable service interfaces and domain entry points: scheduling,
execution, cache, resource, memory, storage, filesystem, traffic/rate
limiting, resilience and distributed capabilities when implemented.

**Does not own:** Algorithm loops, worker pools, mutable registries,
framework adapters or hidden global state.

**May depend on:** `core/` public value types and the JDK.\
**Must not depend on:** `runtime/` implementations, concrete
`algorithms/` classes or `integrations/`.

### `core/` --- Shared contracts and lifecycle primitives

**Owns:** Small domain-neutral value types, shared metadata, correlation
context, lifecycle contracts and base exception/error-code types.

**Does not own:** Algorithms, strategy selection, worker pools, caches
or framework adapters.

**May depend on:** The JDK and core-internal types.\
**Must not depend on:** `api/`, `orchestration/`, `runtime/` or
`algorithms/`.

Do not create one universal request/result object with dozens of
nullable fields. Keep domain-specific request and result types in their
appropriate API packages.

### `orchestration/` --- Multi-step request coordination

**Owns:** Orchestrators, planners, resolver coordination, analysis
coordination, validation, constraints and decision records.

**Does not own:** Algorithm implementations, public domain interfaces or
long-lived worker pools.

**May depend on:** `core/`, public contracts, `workload/`, `strategy/`
and algorithm interfaces.\
**Rule:** Use orchestration only when a request genuinely needs
coordination; simple operations should stay simple.

### `strategy/` --- Strategy selection and descriptors

**Owns:** Strategy registry abstractions, factories, descriptors,
capability declarations, selection policies and provider coordination.

**Does not own:** Algorithm logic or consumer business logic.

**Rule:** Selection should use stable interfaces and capabilities, not
hard-coded knowledge of every concrete implementation. Support explicit
selection, documented rule-based automatic selection, and custom
strategies only through a stable SPI. Automatic selection is not a
guarantee of global optimality.

### `workload/` --- Workload descriptions

**Owns:** Immutable workload profiles, analyzers, classifiers,
characteristics and declared requirements.

**Does not own:** Task execution or unsupported assumptions about remote
systems.

**Rule:** Derive characteristics only from supplied data or reliable
measurements. Do not pretend to know actual execution time, remote
health or network congestion without evidence.

### `algorithms/` --- Algorithm implementations

**Owns:** Domain algorithms under `scheduling/`, `synchronization/`,
`deadlock/`, `memory/`, `paging/`, `disk/`, `filesystem/`, `hashing/`,
`rate/`, `resilience/`, `distributed/` and `collections/`.

**Does not own:** Public facades, application orchestration, framework
integration or global lifecycle.

**Rule:** Prefer JDK types and small domain contracts. Separate: -
**Pure calculations:** input to calculated output, without hidden
mutable state. - **Stateful algorithms:** cache entries, tokens or
breaker state; thread safety must be explicit. - **Runtime services:**
scheduling, queueing, timers and lifecycle around an algorithm.

Algorithm correctness must not depend on a metrics backend.

### `execution/` --- Task execution machinery

**Owns:** Engine, workers, bounded queues, dispatching, executor
implementations/adapters and execution lifecycle.

**Does not own:** Public domain policies or application-wide static
executors.

**Rule:** Define queue capacity, rejection policy, task state
transitions, cancellation, shutdown and ownership. Depend on core
lifecycle/task contracts and JDK concurrency; do not depend on API
factory classes.

### `runtime/` --- Stateful operational services

**Owns:** Runtime implementations for scheduling, processes, resources,
memory, cache, storage, filesystem, traffic, network, resilience and
distributed services.

**Does not own:** Public API definitions or duplicate algorithm logic.

**May depend on:** `core/`, algorithms and execution abstractions.\
**Rule:** Public API types must not expose concrete runtime
implementation classes. Anything creating threads, timers or queues
needs explicit lifecycle/cleanup semantics.

### `metrics/` --- Optional measurement primitives

**Owns:** Collectors, counters, timers, histograms, registry
abstractions and snapshots.

**Does not own:** Domain decisions or mandatory vendor integrations.

**Rule:** Core correctness must work with metrics disabled. Avoid
unbounded per-request metric labels/storage and keep overhead
configurable.

### `diagnostics/` --- Decision explanations and health

**Owns:** Decision explanations, optional trace events, health reports
and derived statistics.

**Does not own:** Mandatory logging backend, secrets or unlimited
retention of task inputs/results.

**Rule:** Diagnostics must explain decisions without calling back to
mutate the decision being explained.

### `spi/` --- Supported extension contracts

**Owns:** Stable interfaces for custom strategy, algorithm, execution
and metrics providers.

**Does not own:** Internal planner classes, mutable registry
implementation or worker internals.

**Rule:** SPI depends only on stable API/core types and the JDK, not on
`runtime/` or concrete algorithm implementations. Once published, SPI is
public API and follows compatibility/versioning rules.

### `integrations/` --- Optional adapters

**Owns:** Optional Spring integration, standard Java executor adapters
and other explicitly supported integrations.

**Does not own:** Core algorithms or features that require a framework
to work.

**Rule:** Integrations depend inward on public API/SPI. Core must never
import integrations. If an integration introduces consumer dependencies,
isolate it in an optional Maven artifact when justified.

### `testing/` --- Test support

**Owns:** Fixtures, input generators, assertions and stress helpers.

**Rule:** Test frameworks and fixtures must not leak into normal runtime
dependencies. Production code must never depend on `testing/`.

### Supporting project areas

-   `benchmarks/`: JMH benchmarks; not part of the consumer runtime JAR.
-   `examples/`: runnable consumer examples.
-   `docs/` or `documentation/`: choose one name and use it
    consistently.
-   `build/`: only if build-support scripts genuinely need a dedicated
    folder.

The agreed **14 principal project areas** are the 13 Java package roots
above plus `benchmarks/`. Examples, documentation and optional build
support are repository-level support directories, not runtime package
roots.

## 4. Dependency direction

Dependencies should point inward toward stable contracts.

  -----------------------------------------------------------------------
  From                    May depend on           Must not depend on
  ----------------------- ----------------------- -----------------------
  `api/`                  `core/`, JDK            Concrete
                                                  runtime/algorithm
                                                  implementations,
                                                  integrations

  `core/`                 JDK, core types         API, orchestration,
                                                  runtime, algorithms

  `orchestration/`        Core, public contracts, Framework integrations
                          workload, strategy,     
                          algorithm interfaces    

  `strategy/`             Core, stable API/SPI    Runtime implementation
                          contracts               details where avoidable

  `workload/`             Core, relevant public   Runtime, framework
                          request types           adapters

  `algorithms/`           JDK, small domain       Integrations,
                          contracts               high-level
                                                  orchestration

  `execution/`            Core, JDK concurrency,  API facade factories,
                          lifecycle contracts     integrations

  `runtime/`              Core, algorithms,       Integrations and
                          execution abstractions  consumer application
                                                  code

  `metrics/`              Core, JDK               Algorithm decisions,
                                                  runtime implementation
                                                  classes

  `diagnostics/`          Core,                   Callbacks that mutate
                          metrics/diagnostic      decisions
                          contracts               

  `spi/`                  Stable API/core         Runtime and concrete
                          contracts, JDK          implementation packages

  `integrations/`         Public API/SPI and      Core importing
                          optional framework      integrations

  `testing/`              Public API and test     Production packages
                          dependencies            

  `benchmarks/`           Public API and JMH      Production packages
                                                  depending on benchmark
                                                  tools
  -----------------------------------------------------------------------

### Avoid cycles

Bad: `api -> runtime -> api`\
Better: `api -> core contracts <- runtime implementation`

Bad: `algorithms -> orchestration -> algorithms`\
Better: orchestration depends on an algorithm interface; the
implementation implements that interface.

Bad: `core -> integrations -> core`\
Better: integrations depend on core/public API; core does not know
integrations exist.

If a lower-level package needs a higher-level capability, introduce a
narrow interface at an appropriate lower-level boundary rather than
allowing a reverse dependency.

## 5. Public API versus implementation visibility

  -----------------------------------------------------------------------
  Type                    Recommended visibility  Reason
  ----------------------- ----------------------- -----------------------
  Domain service          Public                  Consumer entry point
  interface                                       

  Request/config/result   Public when required by Defines usage
                          the contract            

  Strategy                Public if consumers     Explicit configuration
  enum/identifier         select it               

  SPI interface           Public only when        Extension contract
                          supported               

  Concrete algorithm      Internal by default;    Avoid locking
  class                   public only for a       implementation into API
                          documented use case     

  Planner/resolver        Internal                Can evolve safely
  implementation                                  

  Worker/queue            Internal unless         Prevent coupling
  implementation          intentionally exposed   

  Metrics registry        Internal                Avoid reliance on
  internals                                       storage details

  Test fixture            Test-only or separate   Prevent runtime leakage
                          test artifact           

  Framework adapter       Public only in optional Keep core
                          integration             dependency-free
  -----------------------------------------------------------------------

Public visibility is not a substitute for a well-designed extension
point. Expose a stable interface where substitution is genuinely needed.

## 6. Request flow by service type

### Pure scheduling calculation

``` text
ScheduleRequest
 -> validate tasks and policy
 -> derive workload features if required
 -> resolve explicit/automatic policy
 -> calculate schedule
 -> ScheduleResult + optional explanation
```

No worker pool is required for a pure plan calculation.

### Cache lookup

``` text
Cache.get(key)
 -> validate key
 -> consult cache state
 -> check expiry
 -> update hit/miss stats if enabled
 -> return Optional/value
```

Do not run a general strategy planner for every lookup. Policy selection
normally happens when the cache is configured.

### Asynchronous execution

``` text
TaskRequest
 -> validate lifecycle and options
 -> enforce queue capacity/rejection policy
 -> create TaskHandle
 -> enqueue and dispatch
 -> worker runs task
 -> record completion/failure/cancellation
 -> release resources
 -> complete handle/update status
```

Document whether queue waiting counts toward a timeout, how cancellation
works, and what happens when the queue is full.

### Retry and circuit breaker

``` text
Operation + policy
 -> validate attempts/durations
 -> check circuit admission if enabled
 -> run operation
 -> classify outcome
 -> retry eligible failures within limits
 -> update breaker state
 -> typed result or documented exception
```

Define the composition order. Never allow unbounded retries or retries
beyond an overall deadline.

## 7. Automatic strategy selection

Automatic selection is a policy component, not magic.

**Permitted inputs:** user constraints, supplied workload
size/distribution, supported strategy capabilities, configured resource
limits, and reliable runtime measurements when deliberately supported.

**Selection steps:** 1. Validate request and required capabilities. 2.
Remove strategies that cannot satisfy constraints. 3. If explicit
strategy is selected, use it or return a clear incompatibility. 4.
Otherwise apply documented deterministic rules. 5. Return selected
strategy and reason. 6. If no strategy fits, return a typed
unsupported/incompatible result.

Phase-1 rules: deterministic and testable; no unsupported claims of
optimality; no invented workload measurements; explicit fallback
behaviour; custom strategies only through a stable SPI.

## 8. Architecture controls

1.  Each package has one clear responsibility.
2.  No empty placeholder packages.
3.  No circular imports; add architecture tests where practical.
4.  Prefer constructor/factory injection over a service locator
    everywhere.
5.  Avoid hidden global executors and mutable singletons.
6.  Framework dependencies never leak into core signatures.
7.  Keep internal classes package-private when possible.
8.  Test utilities and benchmark libraries do not enter runtime scope.
9.  Avoid duplicating the same model across API, core and runtime.
10. Create interfaces at meaningful substitution/extension boundaries,
    not for every trivial class.

Build checks should include Maven compile/test, dependency tree review,
package/API visibility review, algorithm unit tests, runtime integration
tests, and a sample consumer that uses the packaged artifact.

## 9. Maven module decision

Start with **one Maven artifact**. Fourteen logical areas do not imply
fourteen Maven modules. One artifact reduces release coordination and
dependency complexity while contracts are still evolving.

Split later only when consumers need dependency isolation or optional
integrations bring real dependency costs. Possible future artifacts
could include `unircore-core`, `unircore-scheduling`, `unircore-cache`,
`unircore-resilience`, or `unircore-spring-boot-starter`. These are
possibilities, not Phase-1 commitments.

## 10. Architecture decision register

  ------------------------------------------------------------------------
  Decision                Recommended default      Revisit when
  ----------------------- ------------------------ -----------------------
  Maven layout            One artifact, standard   Real dependency
                          source/test roots        isolation is needed

  Public API              Stable interfaces and    Consumer feedback
                          typed domain models      indicates usability
                                                   problems

  Orchestration           Only for multi-step      A simple call becomes
                          decisions                unnecessarily complex

  Strategy selection      Explicit or              Reliable measurements
                          deterministic rule-based support an adaptive use
                                                   case

  Algorithms              Separate pure            A domain requires
                          calculation from         different semantics
                          stateful runtime         

  Execution               Bounded queues and       Workloads justify
                          explicit rejection       another policy

  Metrics                 Optional, low-overhead   Consumers need an
                          abstractions             adapter

  SPI                     Small, documented,       Validated extension use
                          compatibility-governed   cases emerge

  Integrations            Optional and isolated    A specific integration
                                                   is required

  Test support            Not part of runtime      A supported test
                          artifact                 artifact is justified
  ------------------------------------------------------------------------

## 11. Phase-2 acceptance checklist

-   [ ] Each package has a defined responsibility.
-   [ ] Public contracts are separate from implementation classes.
-   [ ] Dependency directions are documented and reviewed.
-   [ ] No circular package dependencies exist.
-   [ ] `core/` does not import higher-level packages.
-   [ ] `api/` does not expose concrete runtime internals.
-   [ ] Algorithms are independent of optional frameworks.
-   [ ] Pure calculations are distinct from stateful/asynchronous
    services.
-   [ ] Automatic selection is deterministic and explainable in Phase 1.
-   [ ] SPI contains only intentionally supported extension contracts.
-   [ ] Thread ownership and lifecycle are explicit.
-   [ ] Metrics/diagnostics can be disabled or kept low-overhead.
-   [ ] Test utilities and JMH do not leak into runtime dependencies.
-   [ ] The agreed 14 principal project areas are represented without
    forcing empty packages.
-   [ ] One Maven artifact remains the default unless isolation requires
    more.

## 12. Remaining topics after this document

This file covers **Topic 2: Internal architecture and module
boundaries**. Still pending:

3.  **Correctness, validation and error handling** --- invalid inputs,
    exception hierarchy, impossible requests, timeouts, cancellation,
    cleanup and resource limits.
4.  **Algorithm contracts and strategy selection** --- supported
    algorithms, constraints, time/space complexity, thread safety,
    workload analysis, explicit/automatic selection and SPI details.
5.  **Testing and performance standards** --- unit, integration,
    concurrency, stress, regression, cross-platform and API
    compatibility testing; JMH and performance-claim rules.
6.  **Scope and implementation roadmap** --- first-release algorithm
    families, priorities, deferred features and production-ready
    criteria.
7.  **Documentation and consumer experience** --- README, JavaDoc,
    examples, installation, selection guides, migration, troubleshooting
    and contribution rules.
8.  **Observability and operational behaviour** --- metrics,
    diagnostics, tracing, optional logging, overhead and retention
    limits.

The Phase-1 public API specification and deployment/hosting/versioning
plan already exist. After Topics 3--8 are agreed, reconcile the
documents into one implementation baseline before coding.

## 13. References

-   Java API: https://docs.oracle.com/en/java/javase/17/docs/api/
-   Maven dependency mechanism:
    https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
-   Semantic Versioning: https://semver.org/
-   JMH: https://github.com/openjdk/jmh
