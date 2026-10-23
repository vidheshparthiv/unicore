# UnirCore --- Constraints, Thresholds, and Validation Methodology

**Status:** Proposed engineering assurance plan. Numerical defaults must
be measured and reviewed before release.\
**Purpose:** Define how limits are selected, how thresholds are
validated, which metrics are used, and what evidence is needed before
claiming reliability or performance.

## 1. Central rule

Do not invent a threshold and call it optimal. Classify every value as:

1.  **Correctness constraint** --- required by the algorithm or
    contract. Example: Round Robin quantum must be positive.
2.  **Safety default** --- conservative limit intended to prevent
    unbounded resource growth. Example: finite task queue.
3.  **Performance-tuning parameter** --- workload-dependent value
    selected through measurement. Example: worker count or cache
    capacity.

Passing tests on one machine cannot guarantee that every consumer
workload will behave identically. The goal is to define the supported
envelope, validate the implementation across representative cases,
publish limits, and help consumers tune within them.

## 2. Constraint categories

### 2.1 Hard correctness constraints

Examples: - Time quantum \> 0. - Retry maximum attempts \>= 1, with the
initial-attempt definition explicit. - Token-bucket capacity \> 0;
refill rate \> 0 if a replenishing bucket is required. -
Durations/timeouts are non-negative; methods that require a positive
wait must reject zero as documented. - Node IDs are unique within a node
set. - Weights follow the chosen algorithm's documented valid range. -
Job burst/remaining-time values are non-negative. - A deadline is not
extended by retrying. - A terminal task state cannot transition back to
`RUNNING`. - A strategy must satisfy all mandatory capabilities before
selection.

These are enforced at public/configuration boundaries and tested at
boundary values.

### 2.2 Safety/resource constraints

Examples: - Maximum execution threads and bounded queue capacity. -
Bounded completed-task history. - Maximum retry attempts and delay. -
Cache capacity. - Diagnostic retention and metrics dimensions. - Maximum
accepted batch size where needed. - Bounded half-open circuit-breaker
trials.

Limits should have safe defaults, allowed override ranges, and visible
failure/rejection behavior. Silent unbounded growth is not acceptable.

### 2.3 Workload-dependent tuning parameters

Examples: - Worker count, queue capacity, cache capacity/TTL. - Rate and
burst capacity. - Retry delay/multiplier/jitter. - Circuit-breaker
threshold/window. - Round Robin quantum in a simulation. - Node weights
and diagnostic retention.

A default is only a starting point until measured against the consumer's
workload, service objectives, and resource budget.

## 3. Initial validation matrix

  -------------------------------------------------------------------------
  Parameter               Correctness rule          How a default should be
                                                    chosen
  ----------------------- ------------------------- -----------------------
  Burst time              Non-negative; required by Input contract; never
                          strategies that need an   invent a runtime
                          estimate                  

  Round Robin quantum     Positive; unit explicit   Simulation/workload
                                                    study, not universal
                                                    optimum

  Priority                Valid under documented    Define whether lower or
                          representation            higher value wins

  Cache capacity          Explicit zero/minimum     Working set and memory
                          semantics                 budget; measure hit
                                                    ratio and footprint

  TTL                     Non-negative; monotonic   Data freshness
                          elapsed time              requirement and expiry
                                                    tests

  Token capacity          Positive integer          Maximum acceptable
                                                    burst

  Refill rate             Positive for a            Allowed average
                          replenishing bucket       operation rate

  Retry attempts          At least one total        Error budget, deadline,
                          attempt                   and idempotency

  Backoff                 Non-negative, bounded,    Recovery behavior and
                          overflow-safe             total deadline

  Breaker threshold       Valid                     False-open vs late-open
                          count/percentage/window   trade-off
                          range                     

  Half-open trials        Positive and bounded      Recovery probe load

  Node weight             Positive unless           Consumer's traffic
                          zero-weight is explicitly allocation; validate
                          supported                 distribution

  Worker count            Positive and bounded      CPU/I/O profile and
                                                    downstream capacity

  Queue capacity          Finite; saturation        Burst tolerance,
                          behavior explicit         queue-wait budget,
                                                    memory cost

  Diagnostic retention    Finite                    Troubleshooting need
                                                    and memory budget

  Deadline                Non-negative; monotonic   Caller SLA/overall
                          arithmetic                operation budget
  -------------------------------------------------------------------------

## 4. Threshold-selection process

### Step 1 --- Define the workload

Record expected workload size/distribution, typical and peak rate,
CPU-bound versus I/O-bound behavior, memory budget, acceptable queue
wait/latency, error budget, retry safety, fairness/cancellation needs,
local versus distributed scope, and cache freshness requirements.

If requirements are unknown, the default is provisional---not
evidence-based optimality.

### Step 2 --- Establish a baseline

1.  Create a reference workload.
2.  Verify correctness against expected results.
3.  Measure time, allocation, memory growth, and output quality.
4.  Record hardware, OS, JDK/JVM flags, workload, and configuration.
5.  Repeat runs to estimate variance.
6.  Change one parameter at a time where practical.
7.  Compare against the same baseline.

### Step 3 --- Optimize for an explicit objective

-   **Queue capacity:** derive from tolerated burst, queue-wait budget,
    and memory cost.
-   **Retry attempts:** ensure worst-case per-attempt duration plus
    delays fits the overall deadline.
-   **Cache capacity:** measure hit ratio and memory over representative
    access traces.
-   **Circuit breaker:** evaluate false-open and late-open behavior
    using controlled failure sequences, then realistic failure patterns.
-   **Worker count:** measure throughput, tail latency, queue growth,
    and downstream saturation across a controlled range.
-   **Rate limit:** set capacity from allowed burst and refill rate from
    permitted average rate.

### Step 4 --- Record evidence

For every default, record value, classification, rationale,
workload/environment, trade-offs, conditions for changing it, and tests
protecting the behavior.

## 5. Evaluation metrics by family

### 5.1 Scheduling

**Correctness:** valid job coverage, no prohibited overlap, valid
timeline intervals, correct formulas, deterministic tie-breaks where
promised.\
**Quality/performance:** average waiting time, turnaround time, response
time, maximum waiting/starvation indicators, computation time, memory.

These metrics can conflict: a policy may reduce average waiting while
worsening fairness or maximum waiting.

### 5.2 Cache

**Correctness:** hit/miss correctness, eviction order, capacity
invariant, expiry boundary, concurrency/statistics invariants.\
**Metrics:** hit ratio = hits / (hits + misses), with defined
zero-denominator behavior; latency distribution, throughput,
allocations, memory at capacity, eviction/expiry cost, contention.

High hit ratio alone does not prove suitability; stale data, memory use,
or contention can still be harmful.

### 5.3 Rate limiting

**Correctness:** permit accounting, monotonic refill, capacity,
immediate-denial/waiting semantics, timeout/interruption behavior.\
**Metrics:** acquisition throughput, contention latency, CPU/lock
contention, utilization, denial rate, timing accuracy within documented
tolerance.

Do not compare token bucket and fixed window as if they enforce
identical burst behavior.

### 5.4 Retry and circuit breaker

**Correctness:** attempts never exceed cap; non-retryable errors stop;
deadline/interruption preserved; breaker transitions valid; open state
does not invoke callback; half-open concurrency is bounded.\
**Metrics:** total duration including delays, attempts per logical
operation, recovery success rate, retry amplification, false-open rate,
detection delay, time in each state.

More retries are not automatically better; they can amplify a failing
dependency.

### 5.5 Load balancing

**Correctness:** selected node is eligible, no ineligible node is
selected, IDs/weights validated, snapshots coherent.\
**Metrics:** selection latency, distribution error over a defined
sample, fairness, update responsiveness, concurrent contention.

Weighted distribution tests must state sample size and tolerance.

### 5.6 Execution

**Correctness:** legal task states, documented queue saturation,
shutdown contract, best-effort cancellation, bounded history.\
**Metrics:** throughput, queue-wait latency, execution latency
separately, rejection rate, CPU/memory, p95/p99 latency, throughput
across thread/queue settings.

More threads or a larger queue do not guarantee better performance.

## 6. Testing design for confidence

### 6.1 Layers

-   Unit tests for algorithm logic and validation.
-   Reusable contract tests across implementations.
-   Integration tests for API → runtime → algorithm → result.
-   Concurrency tests for promised thread safety.
-   Stress tests for saturation and bounded resource behavior.
-   Regression tests for every confirmed defect.
-   Packaging/compatibility tests against the actual JAR and minimum
    supported JDK.

### 6.2 Boundary values

For every numeric configuration test minimum valid, one below minimum,
zero, typical, upper bound, one beyond, overflow-sized input, and
null/missing where applicable. For TTL/deadlines test just before, at,
and just after the boundary.

Use a controlled clock rather than arbitrary sleeps whenever possible.

### 6.3 Invariant/property tests

Examples: - Cache size never exceeds configured capacity after a
completed operation. - Retry attempts never exceed the maximum. - Token
count remains within \[0, capacity\] for the chosen non-reservation
token bucket. - Selected node belongs to the eligible snapshot. -
Scheduling metrics match the timeline. - Task state never returns from
terminal to running. - Breaker state is always legal.

Use property-based test tooling only if it provides meaningful value; do
not add dependencies for appearance.

### 6.4 Concurrency tests

Use barriers, latches, controlled executors, and repeated race tests.
Assert invariants, not thread order unless guaranteed. Stress tests are
evidence, not proof that all races are impossible.

### 6.5 Independent reference checks

For deterministic algorithms, include hand-calculated cases or an
independent simple test reference. Avoid validating one implementation
only against helpers that share the same potential defect.

## 7. Performance testing

Use JMH for JVM microbenchmarks. Separate setup from measured work
unless setup is the target. Record warm-up, measurement iterations,
forks where appropriate, workload distribution, thread count, JDK/JVM
settings, and allocation data.

Process: 1. Run correctness tests first. 2. Benchmark small, typical,
and large workloads. 3. Include low- and high-contention cases for
concurrent services. 4. Repeat runs and report variance. 5. Compare
equivalent semantics/configurations. 6. Inspect allocation and memory as
well as throughput. 7. Keep noisy benchmark thresholds informational
until the environment is controlled. 8. Add CI performance gates only
after variance and regression thresholds are understood.

A developer laptop benchmark is not a guarantee of end-user performance.

## 8. How to avoid overclaiming reliability

UnirCore cannot prove every external workload will succeed. Build
credible confidence by: 1. Specifying supported behavior. 2. Validating
public inputs. 3. Testing invariants and boundaries. 4. Testing promised
concurrency behavior. 5. Stressing saturation and resource limits. 6.
Benchmarking representative workloads and publishing conditions. 7.
Testing supported JDK/platform combinations. 8. Releasing a small stable
feature set before expanding. 9. Handling user feedback without
capturing sensitive payloads. 10. Protecting behavior with regression
tests and semantic versioning.

Passing tests means tested properties held in tested cases/environment;
it does not mean failure is impossible.

## 9. Default-value policy

Every value must be classified as: - **Hard invariant:** not
configurable if changing it breaks correctness. - **Safety default:**
configurable within safe bounds. - **Workload tuning parameter:**
consumer-specific, with a measurement procedure. - **Deferred:** no
value until the feature is designed.

For example, queue capacity should be finite and configurable, but its
numeric default must come from expected memory cost, burst size,
queue-wait budget, and saturation tests---not from an unrelated
library's default.

## 10. Threshold decision record

For every tunable parameter record: - Name and owning
configuration/class. - Classification: correctness, safety, or
performance. - Default and allowed range. - Reason for bounds. -
Workload assumption or requirement. - Evaluation metrics. -
Test/benchmark environment. - Boundary tests. - Failure behavior when
exceeded. - Whether consumers can override it. - Known trade-offs and
review trigger.

## 11. Release acceptance checklist

-   [ ] Every numeric parameter has a type, unit, valid range, and
    overflow policy.
-   [ ] Every default is classified.
-   [ ] No arbitrary threshold is described as optimal without evidence.
-   [ ] Boundary tests cover min/zero/max/overflow.
-   [ ] Time-based logic uses monotonic elapsed time.
-   [ ] Retry limits fit the deadline budget.
-   [ ] Queue/cache/diagnostic/task history is bounded.
-   [ ] Thread-safety claims have concurrency tests.
-   [ ] Metrics have formulas and edge-case definitions.
-   [ ] Benchmarks document environment and workload.
-   [ ] User-facing performance claims are scoped to tested conditions.
-   [ ] Known limitations and unsupported workloads are documented.

## Final rule

Thresholds are justified by correctness rules, resource budgets, or
measured workloads---not intuition alone. A strong test suite increases
confidence but cannot guarantee every external workload will perform
identically. UnirCore must expose its limits, test invariants, and avoid
promises its evidence cannot support.
