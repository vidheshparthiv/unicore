# UnirCore Phase 3 --- Correctness, Validation and Error-Handling Contract

**Status:** Proposed contract for review before implementation\
**Purpose:** Define how UnirCore validates requests, represents expected
outcomes, reports failures, handles timeouts/cancellation, bounds
resources and cleans up state.\
**Applies to:** All public APIs and runtime services, including
scheduling, execution, cache, rate limiting, resilience, resource
management and load balancing.

> This document defines the intended behaviour. A feature must not be
> advertised as supported until its implementation and tests satisfy the
> applicable contract.

------------------------------------------------------------------------

## 1. Core rules

1.  **Reject invalid input early.** Validate configuration during
    construction where possible, and validate request-dependent
    conditions before doing work.
2.  **Do not confuse rejection with failure.** A rate-limit denial, full
    queue, open circuit or unavailable resource may be an expected
    outcome represented by a typed result.
3.  **Use exceptions for misuse and exceptional execution failures.**
    Invalid configuration, unsupported combinations and lifecycle misuse
    should produce documented exceptions; expected runtime decisions
    should normally be typed outcomes.
4.  **Never return a fake success.** Do not silently swallow exceptions,
    return default values after failure, or catch fatal JVM errors as
    normal failures.
5.  **Bound resources by default.** Queues, retries, waits, caches and
    retained diagnostics must not grow without a defined limit.
6.  **Make timing semantics explicit.** Every timeout says what it
    limits; every blocking method is clearly named and documented.
7.  **Clean up reliably.** Release leases, cancel timers, remove task
    registrations and close owned resources on both success and failure.
8.  **Preserve useful failure context.** Keep the cause, operation ID
    and safe diagnostic details without exposing secrets or huge
    payloads.
9.  **Keep failure behaviour consistent.** Similar APIs should use
    consistent status names, exception categories and cancellation
    semantics.
10. **Do not promise impossible guarantees.** A timeout cannot safely
    force arbitrary user code to stop, and a thread interrupt does not
    prove that work has terminated.

------------------------------------------------------------------------

## 2. Validation layers

Validation should occur at the earliest layer that has enough
information to perform it.

### 2.1 Configuration-time validation

Run when a configuration/builder is built or a service is created.

Examples: - Worker count must be positive. - Queue capacity must be
positive unless an explicitly documented unbounded mode is supported;
recommended default is bounded only. - Cache maximum size must be
valid. - Retry attempt count must be within a documented range. -
Durations must not be negative. - Rate capacity/refill values must be
valid and internally consistent. - Circuit-breaker thresholds and open
durations must be valid. - A policy requiring a parameter must reject
configurations that omit it.

Fail with `InvalidConfigurationException` (or a more specific subclass)
and identify the offending field.

### 2.2 Request-time validation

Run when an operation is invoked.

Examples: - Required task list is not null or empty. - Task IDs are
unique when the operation requires uniqueness. - Burst time or work
estimate is valid for the chosen scheduling model. - Permit count is
positive and does not exceed a documented maximum. - Cache key is valid
under the cache's null policy. - Resource type and lease are valid. - A
load-balancing request has at least one eligible target. - A
timeout/deadline is compatible with the selected operation.

Use a clear validation failure with the field/constraint and safe
context. Do not start background work before request validation
finishes.

### 2.3 Capability/constraint validation

The request can be syntactically valid but impossible for the selected
strategy.

Examples: - A selected algorithm cannot satisfy a required constraint. -
A policy requires workload estimates that were not supplied. - No
candidate node is eligible. - The requested permits exceed the maximum
capacity. - The runtime has been closed. - An algorithm is not available
in the current edition/version.

Return a documented typed incompatibility/unsupported outcome when this
is an expected selection result; use a documented exception when the
caller has made an invalid API/configuration choice. The distinction
must be consistent per API.

### 2.4 Execution-time failures

Failures arising after valid work begins: - User-supplied callable
throws. - Worker rejects or cannot start work. - Resource acquisition
times out. - An operation exceeds its timeout. - An external dependency
fails. - A cleanup operation fails.

Preserve the original cause where applicable. Do not report a failed
operation as successful because an error was caught.

------------------------------------------------------------------------

## 3. Validation conventions

Recommended conventions:

-   Use `Objects.requireNonNull` or equivalent checks for required
    references, but wrap errors in domain-specific validation where the
    public contract benefits from a clear field name.
-   Reject negative durations and invalid numeric ranges; do not
    silently clamp unless the API explicitly says it normalizes values.
-   Avoid implicit defaults for critical controls such as retry count,
    queue capacity or timeouts. If defaults exist, document them in
    JavaDoc and the README.
-   Validate enum/configuration combinations together.
-   Use immutable configuration objects.
-   Do not mutate caller-owned collections or task lists unexpectedly.
    Copy or snapshot them when needed.
-   Do not validate the same expensive property repeatedly on a hot path
    unless the data may change.
-   Avoid logging invalid inputs verbatim if they may contain
    credentials, personal data or large payloads.
-   Include the field name, expected constraint and actual safe value
    where useful.

Example:

``` java
ExecutionConfig config = ExecutionConfig.builder()
    .workers(0)
    .queueCapacity(200)
    .build();
```

Recommended outcome: `InvalidConfigurationException` identifying
`workers` and stating that the value must be greater than zero. It
should not wait until the first task is submitted to discover this
configuration error.

------------------------------------------------------------------------

## 4. Exception hierarchy

Keep the hierarchy small and meaningful. Avoid creating dozens of
exception classes that differ only by name.

``` text
RuntimeException
└── UnirCoreException
    ├── InvalidConfigurationException
    ├── ValidationException
    ├── UnsupportedOperationException (UnirCore-specific subtype/name)
    ├── IncompatibleStrategyException
    ├── ServiceClosedException
    ├── ResourceLimitException
    ├── OperationTimeoutException
    ├── TaskExecutionException
    ├── CancellationException (use JDK type where appropriate)
    └── InternalUnirCoreException
```

This is a proposed taxonomy. Avoid naming a custom type exactly like a
JDK exception if it causes confusion; for unsupported operations, a
clearly named `UnsupportedCapabilityException` is preferable.

### Suggested meaning

  ----------------------------------------------------------------------------------
  Exception                          Use when                Example
  ---------------------------------- ----------------------- -----------------------
  `UnirCoreException`                Base for documented     Common catch boundary
                                     UnirCore runtime        
                                     exceptions              

  `InvalidConfigurationException`    Service configuration   Worker count is zero
                                     is invalid              

  `ValidationException`              An invoked request      Duplicate task IDs
                                     violates required input where uniqueness is
                                     rules                   required

  `UnsupportedCapabilityException`   Requested capability is Request asks for an
                                     not                     unsupported policy
                                     implemented/available   

  `IncompatibleStrategyException`    Selected strategy       Policy cannot handle a
                                     cannot satisfy the      required deadline
                                     request constraints     

  `ServiceClosedException`           Operation is attempted  Submit after close
                                     after shutdown          

  `ResourceLimitException`           A configured resource   Queue full, if
                                     limit is reached and    documented for that API
                                     API chooses             
                                     exception-based         
                                     rejection               

  `OperationTimeoutException`        An operation exceeds a  Resource wait expires
                                     defined time bound      

  `TaskExecutionException`           A task fails and the    Callable throws
                                     API chooses an          
                                     exception-based         
                                     retrieval contract      

  `InternalUnirCoreException`        Unexpected internal     Registry invariant
                                     invariant failure that  broken
                                     can be safely surfaced  
  ----------------------------------------------------------------------------------

Do not use the exception hierarchy for every expected decision. Prefer
typed results for rate-limit rejection, circuit-open state, normal task
cancellation and queue rejection when the API is designed to return such
outcomes.

### Error codes

Where errors are serialized, logged, or inspected programmatically,
define stable machine-readable codes such as:

-   `INVALID_CONFIGURATION`
-   `INVALID_REQUEST`
-   `UNSUPPORTED_CAPABILITY`
-   `INCOMPATIBLE_STRATEGY`
-   `SERVICE_CLOSED`
-   `RESOURCE_LIMIT_REACHED`
-   `TIMEOUT`
-   `CANCELLED`
-   `TASK_FAILED`
-   `INTERNAL_ERROR`

Error codes should be stable and documented. Exception class names and
human-readable messages are not substitutes for a machine-readable code.

------------------------------------------------------------------------

## 5. Typed result and status model

Use typed results when an outcome is a normal part of the domain. Do not
force consumers to catch exceptions for expected policy decisions.

### General result states

A small shared status vocabulary may include:

-   `SUCCESS`
-   `REJECTED`
-   `TIMED_OUT`
-   `CANCELLED`
-   `FAILED`
-   `UNSUPPORTED`

Not every result needs every status. For example, a cache lookup can
simply return `Optional<V>`; a scheduling calculation can return a
result or fail validation; a rate limiter should return an allow/deny
decision.

### Example: rate-limit decision

``` java
public interface RateLimitDecision {
    boolean allowed();
    Optional<Duration> retryAfter();
    String reasonCode();
}
```

If the call is denied because there are not enough permits, that is not
an exceptional failure. The caller can decide whether to reject an HTTP
request, enqueue work, or try later.

### Example: task result

``` java
public interface TaskResult<T> {
    TaskStatus status();
    Optional<T> value();
    Optional<Throwable> failure();
    Optional<Instant> startedAt();
    Optional<Instant> completedAt();
}
```

The actual public type must avoid contradictory states. For example, a
failed result must not simultaneously expose a successful value unless
the contract explicitly defines a partial result. Consider a sealed
result hierarchy or constructors that enforce invariants rather than a
freely mutable status/value/failure bag.

### Result invariants

-   `SUCCESS` has a valid success value where the operation returns one.
-   `FAILED` has a failure description/cause when appropriate.
-   `CANCELLED` is not reported as successful.
-   `TIMED_OUT` distinguishes whether work never started, was waiting,
    or was already running if that distinction matters.
-   `REJECTED` explains the policy or capacity reason.
-   Optional fields are absent when unknown, not fabricated.

------------------------------------------------------------------------

## 6. Failure policy by domain

  ------------------------------------------------------------------------------
  Domain                  Expected outcome        Recommended representation
  ----------------------- ----------------------- ------------------------------
  Scheduling              Invalid input or no     Validation exception or typed
                          valid plan              infeasible-plan result, chosen
                                                  consistently

  Execution queue         Queue at capacity       `SubmissionResult`/rejection
                                                  outcome, or documented
                                                  `ResourceLimitException`; do
                                                  not mix silently

  Task execution          User callable throws    Failed task handle/result
                                                  preserving cause; optionally
                                                  exception from explicit
                                                  blocking retrieval

  Cancellation            Task cancelled before   `CANCELLED`; report if
                          start                   cancellation was accepted

  Cancellation            Task already completed  Cancellation result says
                                                  cancellation did not occur

  Cache                   Key absent/expired      `Optional.empty()` or cache
                                                  miss result

  Cache                   Capacity reached        Apply eviction policy; if
                                                  impossible, return documented
                                                  write outcome

  Rate limiter            Permits unavailable     Denied decision with reason
                                                  and possible retry duration

  Retry                   All attempts fail       `RetryResult` with attempt
                                                  count and final cause, or
                                                  documented exception contract

  Circuit breaker         Circuit is open         Typed `CircuitOpen`
                                                  decision/result; no remote
                                                  call made

  Resource manager        Capacity unavailable    Typed timeout/rejection or
                          until timeout           documented timeout exception

  Load balancer           No eligible nodes       Typed no-target result or
                                                  documented exception,
                                                  consistently chosen

  Diagnostics             Metrics disabled        Empty/unsupported result; must
                                                  not break the primary
                                                  operation
  ------------------------------------------------------------------------------

For each public method, choose one failure style and document it. Avoid
APIs where the same condition returns a result in one overload and
throws an undocumented exception in another.

------------------------------------------------------------------------

## 7. Timeouts, deadlines and clocks

### Distinguish time concepts

-   **Queue timeout:** maximum time a submitted task may wait before
    starting.
-   **Execution timeout:** maximum intended execution duration after
    work starts.
-   **Overall deadline:** latest time by which the entire operation
    should finish, including queueing/retries where specified.
-   **Resource-acquisition timeout:** maximum wait to obtain a
    lease/permit.
-   **Retry delay:** planned pause between attempts.
-   **TTL:** cache entry validity duration.
-   **Shutdown grace period:** time allowed for in-flight tasks to
    complete.
-   **Circuit-open duration:** period before a recovery probe may be
    attempted.

Do not use a single ambiguous `timeout` field when the service needs to
distinguish these concepts.

### Clock rules

-   Use `Duration` for durations and `Instant` for externally meaningful
    timestamps.
-   Use a monotonic source such as `System.nanoTime()` for elapsed-time
    and timeout arithmetic.
-   Do not use wall-clock timestamps as the only basis for measuring
    elapsed time.
-   Reject negative durations; document zero-duration behaviour.
-   Make time sources injectable internally so TTL, retry and breaker
    transitions can be tested deterministically.
-   Do not promise exact millisecond scheduling: JVM and
    operating-system scheduling are not hard real-time guarantees.

### Timeout behaviour

Every timeout contract must answer: 1. Does it include queue waiting? 2.
Does it include retry delays? 3. Is the task interrupted or merely
marked timed out? 4. Can user code ignore interruption? 5. What
state/result does the caller observe? 6. Is the timeout best-effort or a
strict admission deadline?

Recommended rule: a timeout bounds how long UnirCore waits or permits an
operation; it cannot safely guarantee that arbitrary user code has
physically stopped.

------------------------------------------------------------------------

## 8. Cancellation contract

Cancellation is a request to stop work, not a universal guarantee.

### Recommended task states

``` text
CREATED -> QUEUED -> RUNNING -> SUCCEEDED
                     |   |  -> FAILED
                     |   └──> CANCELLATION_REQUESTED -> CANCELLED (if confirmed)
QUEUED -> CANCELLED
QUEUED -> REJECTED
QUEUED/RUNNING -> TIMED_OUT (per timeout policy)
```

The implementation may use a smaller state set, but transitions must be
documented and thread-safe.

### Rules

-   Cancelling a queued task should remove it from the queue where safe
    and release its queued capacity.
-   Cancelling a running task should request interruption/cooperative
    cancellation according to the API.
-   Do not report a running task as fully stopped merely because
    `cancel(true)` or an interrupt was issued.
-   A task that has already completed should return
    `cancellationAccepted = false` or an equivalent outcome.
-   Cancellation callbacks must not execute while holding internal locks
    if that could deadlock or block unrelated tasks.
-   Clean up task registrations and timers once no longer needed.
-   Define how cancellation interacts with retries: normally do not
    start another attempt after cancellation is accepted.
-   Do not automatically retry cancellation or timeout unless the
    configured policy explicitly permits it and the operation is safe to
    repeat.

------------------------------------------------------------------------

## 9. Resource limits and overload behaviour

UnirCore should be bounded by default. Unbounded structures can turn
overload into memory exhaustion.

### Required limits to define

  -----------------------------------------------------------------------
  Component               Limit                   Behaviour when reached
  ----------------------- ----------------------- -----------------------
  Execution queue         Maximum queued tasks    Reject or apply
                                                  documented bounded
                                                  backpressure

  Worker pool             Maximum worker count    Do not create workers
                                                  without a bound

  Task metadata           Retention/cleanup       Remove completed
                          policy                  entries after the
                                                  documented retention
                                                  window

  Retry                   Maximum attempts and    Stop when either bound
                          overall deadline        is reached

  Cache                   Maximum entries/weight  Evict according to
                                                  policy or report failed
                                                  write

  Rate limiter            Maximum capacity/permit Reject invalid or
                          count                   excessive request

  Resource pool           Maximum concurrent      Wait only within a
                          leases                  bounded timeout or
                                                  reject

  Diagnostics             Event                   Drop/summarize with
                          count/retention/size    counters or bounded
                                                  buffer; never grow
                                                  indefinitely

  Metrics                 Label cardinality and   Reject, aggregate or
                          registry size           cap new series
                                                  according to policy

  Batch APIs              Maximum batch size      Validate before
                          where needed            allocating excessive
                                                  memory
  -----------------------------------------------------------------------

### Backpressure choices

For each queue-like API, choose and document one policy: - **Reject
immediately:** predictable and non-blocking. - **Wait up to a timeout:**
bounded blocking, clearly named. - **Caller-runs:** may execute work on
the submitting thread; can cause surprising latency/reentrancy, so do
not make it the default without justification.

Do not silently switch between these policies. Expose the active policy
in configuration and diagnostics.

------------------------------------------------------------------------

## 10. Cleanup and lifecycle

### Ownership model

Every resource must have one clear owner: - If UnirCore creates a worker
pool/timer, the service owns it and must close it. - If the consumer
supplies an executor, ownership must be explicit; UnirCore should not
shut it down unless configured to own it. - If a lease is acquired, the
caller must release/close it. - If a retry is cancelled, pending delay
timers must be cancelled. - If a task finishes, task bookkeeping must be
cleaned up according to documented retention rules.

### Recommended APIs

Services that own resources should implement `AutoCloseable` and
document whether `close()` is idempotent. Use `try-with-resources` for
leases and short-lived services where appropriate.

``` java
try (ResourceLease lease = manager.acquire("database", timeout)) {
    // Use the leased capacity.
}
```

This is illustrative; the actual lease interface should ensure `close()`
has clear release semantics.

### Cleanup failure

-   Do not overwrite the original operation failure with a cleanup
    failure.
-   Preserve cleanup failures as suppressed exceptions or diagnostic
    details where appropriate.
-   Attempt all required independent cleanup actions even if one fails.
-   Avoid running arbitrary consumer callbacks while holding locks.
-   Ensure shutdown is bounded and reports tasks that did not finish
    within the grace period.

------------------------------------------------------------------------

## 11. Thread safety and concurrency invariants

Every public service must document one of: - Thread-safe for concurrent
calls. - Thread-confined to a single thread. - Immutable and safe to
share. - Not thread-safe; external synchronization required.

### Required invariants

-   Configuration objects are immutable after build.
-   State transitions are atomic or synchronized as required.
-   A task is not simultaneously reported as both succeeded and
    cancelled.
-   A lease cannot be released twice without a documented outcome.
-   A cache never returns an expired value as valid.
-   Rate limiter accounting does not create permits from concurrent race
    conditions.
-   Circuit-breaker state transitions respect the configured half-open
    probe limit.
-   Shutdown prevents new work after the service enters its documented
    stopping state.
-   Metrics and diagnostics cannot corrupt primary algorithm state if
    their backend fails.

Prefer narrow locks, concurrent collections and well-defined atomic
state transitions. Do not make a thread-safety claim based solely on
using a concurrent collection; the whole operation may involve multiple
state changes.

------------------------------------------------------------------------

## 12. Retry and circuit-breaker safety

### Retry contract

-   `maxAttempts` includes the first attempt.
-   Retry only configured failure categories.
-   Bound attempts and total elapsed time.
-   Use bounded backoff; optionally add jitter to reduce synchronized
    retries.
-   Preserve the final failure and attempt count.
-   Cancellation stops future attempts.
-   Do not retry interruption/cancellation by default.
-   Document idempotency requirements; retrying a non-idempotent action
    can duplicate side effects.
-   Ensure retry delays do not exceed the overall deadline.

### Circuit-breaker contract

-   State transitions are documented: closed, open and half-open, if
    those states are implemented.
-   An open circuit rejects before invoking the protected operation.
-   Half-open probe concurrency is bounded.
-   Success/failure classification is configurable or explicitly
    defined.
-   Cancellation and caller validation errors should not automatically
    count as dependency failures.
-   Reset operations, if exposed, must be explicit and safe.
-   Metrics/diagnostics may expose transitions but must not control
    them.

### Composition

If retry and circuit breaking are composed, document the order because
it changes what the breaker counts: - breaker outside retry: one logical
operation may count as one outcome; - breaker inside retry: each attempt
may affect breaker statistics.

Choose one default, test it, and allow alternative composition only if
there is a clear use case.

------------------------------------------------------------------------

## 13. Security and information hygiene in failures

-   Never include credentials, access tokens or secret configuration
    values in exception messages.
-   Avoid dumping full task payloads or user data into logs/diagnostics.
-   Bound exception context and diagnostic payload size.
-   Use stable error codes for programmatic handling.
-   Do not expose internal filesystem paths or environment variables
    unnecessarily.
-   Make diagnostic payloads opt-in where they may contain sensitive
    information.
-   Preserve the original cause for debugging without leaking sensitive
    details in public-facing messages.
-   Document whether user callbacks can be invoked during error
    handling.

------------------------------------------------------------------------

## 14. Testing requirements for this contract

Each rule needs tests, not just documentation.

### Validation tests

-   Null/missing required values.
-   Zero, negative, maximum and overflow numeric values.
-   Negative/zero/very large durations.
-   Duplicate identifiers and empty collections.
-   Invalid policy combinations.
-   Unsupported capabilities and incompatible constraints.

### Failure tests

-   User task throws checked/runtime exceptions and errors.
-   Queue is full.
-   Resource pool is exhausted.
-   Service is closed before invocation.
-   Timer/cleanup action fails.
-   Metrics/diagnostics backend throws.
-   Retry attempts are exhausted.
-   Circuit is open and half-open probe limit is reached.

### Timing and cancellation tests

-   Queue timeout before a task starts.
-   Execution timeout after a task starts.
-   Cancellation before start, while running and after completion.
-   Retry cancellation during backoff.
-   Shutdown while tasks are queued/running.
-   TTL expiry with an injected/fake clock.
-   Ensure tests avoid fragile assumptions about exact wall-clock
    milliseconds.

### Concurrency and leak tests

-   Concurrent cache get/put/expire.
-   Concurrent permit acquisition.
-   Concurrent lease acquire/release and double release.
-   Concurrent shutdown and submit.
-   Repeated create/close cycles do not leak threads/timers.
-   Task registry and metrics cardinality remain bounded under load.

------------------------------------------------------------------------

## 15. Phase-3 acceptance checklist

-   [ ] Validation layers and their responsibilities are documented.
-   [ ] Exception hierarchy and stable error codes are agreed.
-   [ ] Each API consistently chooses typed outcomes versus exceptions.
-   [ ] Result invariants prevent contradictory states.
-   [ ] Queue, execution, resource and overall timeouts are
    distinguished.
-   [ ] Cancellation semantics are documented as cooperative/best-effort
    where appropriate.
-   [ ] Every queue, retry, cache, pool and diagnostic buffer has a
    limit or an explicit reason not to.
-   [ ] Service and lease ownership is clear; cleanup is tested.
-   [ ] Thread-safety guarantees are declared for every public service.
-   [ ] Retry idempotency, attempt counting and deadline behaviour are
    defined.
-   [ ] Circuit-breaker transitions and retry composition are defined.
-   [ ] Sensitive information is excluded from public
    errors/diagnostics.
-   [ ] Deterministic tests cover validation, timing, cancellation,
    cleanup and concurrency.
-   [ ] No advertised guarantee exceeds what Java/JVM and the
    implementation can actually provide.

------------------------------------------------------------------------

## 16. Decisions to finalize before implementation

1.  Whether invalid request data throws `ValidationException` or returns
    a validation result for each domain.
2.  Queue-full default: immediate typed rejection or a documented
    exception.
3.  Which timeout fields are separate in the initial `ExecutionConfig`.
4.  Whether `TaskHandle` exposes a `CompletionStage`, blocking
    `await(Duration)`, or both.
5.  The task state model and meaning of `TIMED_OUT` versus `CANCELLED`.
6.  Default cache behaviour when capacity is reached and whether nulls
    are prohibited.
7.  Retryable failure classification and retry/circuit-breaker
    composition order.
8.  Whether diagnostic events are retained in memory and, if so, the
    default bound.
9.  Which services own their executors/timers versus accepting
    consumer-owned resources.
10. Which exception types are part of the stable public API in `1.0.0`.

**Recommendation:** Agree on the failure and lifecycle contracts before
implementing the execution runtime. These rules affect every domain and
are expensive to change after consumers depend on them.

------------------------------------------------------------------------

## References

-   Java `Duration`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html
-   Java `System.nanoTime()`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#nanoTime()
-   Java `AutoCloseable`:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/AutoCloseable.html
-   Java concurrency utilities:
    https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/package-summary.html
-   Semantic Versioning: https://semver.org/
