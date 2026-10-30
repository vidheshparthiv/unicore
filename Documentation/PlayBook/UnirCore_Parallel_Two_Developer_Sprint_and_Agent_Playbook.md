# UnirCore — Parallel Two-Developer Execution Playbook
## Sprint-by-sprint, daily ownership, contracts, branches, and agent instructions

**Purpose:** The operational source of truth for two developers implementing UnirCore in parallel.  
**Team:** Developer 1 (project owner, branch `virus`) and Developer 2 (branch `zebra`).  
**Cadence:** Three-working-day sprints; Sunday is the weekly integration/review day.  
**Planning horizon:** Six 3-day sprints (18 working days), then a stabilization/release sprint. The schedule can expand when acceptance gates fail.  
**Core rule:** Both developers produce implementation or test artifacts every working day. They work in separate owned directories against a pre-agreed contract. They do **not** routinely review each other's in-progress code during the day. The formal joint review happens at sprint boundaries and every Sunday.

---

# 1. Collaboration philosophy

## 1.1 Parallel ownership, shared product

The team is not split into “one writes code and the other checks it.” Both developers are builders. Each owns an end-to-end functional area, including its implementation, tests, and documentation. They collaborate by agreeing on interfaces and observable behavior before implementation, then integrate at planned checkpoints.

### Principles

1. **Contract first, implementation in parallel.** Before a sprint starts, both developers agree on the types, method signatures, invariants, error behavior, and data exchanged between their components.
2. **One owner per file.** A file has one primary owner. The other developer does not edit it without explicit agreement.
3. **No hidden cross-directory coupling.** A component communicates through its public API or an explicitly approved internal contract, never by reaching into another component's private classes.
4. **Build and test your own component every day.** Each developer writes implementation and component tests in their own owned area.
5. **Do not block on reviewing the other person's code.** During the three-day sprint, each person focuses on their own deliverables. The team uses compile-time contracts, mocks/fakes, and consumer examples to keep work parallel.
6. **Joint reviews happen at checkpoints.** The end of each 3-day sprint is an integration checkpoint; Sunday is the broader weekly review. Reviews focus on correctness, interfaces, integration, risks, and decisions—not on monitoring each other's daily coding.
7. **Integration is continuous but controlled.** Each developer pushes daily. The branches may contain incomplete work, but `main` only receives work that passes the agreed merge gate.
8. **Tests are part of implementation.** No “finish code first, test at the end” workflow.
9. **No calendar-driven merging.** If an acceptance gate fails, the team fixes or defers scope rather than merging broken code.
10. **Make decisions visible.** Contract changes and unresolved questions are recorded in `docs/implementation/decisions.md` or an ADR.

## 1.2 How parallel work avoids conflicts

Before each sprint, create a short contract file such as `docs/implementation/contracts/sprint-02.md` containing:
- the files/types each owner will create;
- method signatures and field semantics;
- who owns each shared file;
- expected errors and lifecycle behavior;
- test fixtures and acceptance criteria;
- mock/fake behavior if the other component is not ready.

Each developer then builds independently:
- Developer 1 uses a small fake implementation of Developer 2's contract if needed.
- Developer 2 uses the agreed interface and a stub/fake of Developer 1's contract if needed.
- The fake is test-only and must not become a second production implementation.
- No one changes a shared API unilaterally. A contract change requires a short decision note and coordinated updates.

## 1.3 Definition of a productive day

Every working day, each developer must produce at least one tangible artifact:
- production code;
- component tests;
- contract/interface;
- executable example;
- CI/build/test improvement;
- implementation documentation directly required for their component.

“Attended meetings” or “created empty folders” is not a completed daily deliverable.

---

# 2. Product and scope boundaries

UnirCore is a reusable Java library called inside the consumer application's JVM. It is not a hosted service and does not replace kernel-level OS functions.

For the first MVP, implement in dependency order:
1. Maven/CI foundation and shared contracts.
2. Scheduling models, algorithms, deterministic strategy selection.
3. Execution service and task lifecycle.
4. Bounded cache with approved eviction and optional TTL.
5. Token-bucket rate limiter.
6. Bounded retry executor.
7. Circuit breaker with tested state transitions.
8. Metrics/diagnostics and examples.
9. Load balancing/resource management only if the contract is concrete and the core MVP is stable.
10. Packaging, documentation, consumer test, release rehearsal.

Do not implement distributed locks, leader election, a full storage engine, kernel-level features, or a globally optimal strategy selector as part of the initial MVP unless separately approved.

---

# 3. Branching strategy

## 3.1 Branch meanings

- `main`: stable, integrated, releasable history.
- `virus`: Developer 1's persistent working branch.
- `zebra`: Developer 2's persistent working branch.
- `feature/<short-name>`: optional short-lived branch for a focused change, created from the appropriate owner branch.

Example names:
- `feature/scheduling-contract`
- `feature/scheduler-fcfs-sjf`
- `feature/execution-service`
- `feature/cache-lru-ttl`
- `feature/token-bucket`
- `test/scheduler-invariants`
- `build/ci-quality-gates`

## 3.2 Daily branch routine

**Start of day**
1. Fetch remote updates.
2. Pull the latest approved shared contract/base.
3. Check whether `main` contains new changes that affect your interface.
4. Do not pull another developer's unfinished code merely because it exists. Integrate only when the shared contract is stable and the work is useful.

**During day**
1. Commit in small coherent units.
2. Run local tests before pushing.
3. Push to your own branch at least once daily.
4. Use a draft PR for visibility if helpful; no need to request routine in-progress review.

**Sprint boundary**
1. Freeze feature additions temporarily.
2. Both developers integrate the agreed sprint outputs.
3. Run full build and component/integration tests.
4. Resolve conflicts together.
5. Open PRs to `main` (or first merge owner branches into a dedicated integration PR).
6. Merge only if the sprint gate passes.

## 3.3 Recommended integration model

For a two-person project, the safest model is:
- `virus` and `zebra` are long-lived ownership branches.
- Each developer may use short-lived feature branches from their own branch.
- At each sprint boundary, create an integration PR that combines both completed streams into `main`.
- `main` remains protected; no direct pushes.
- If one branch depends on the other's implementation, merge only the agreed interface/contract commit or use a test fake until integration.

Avoid merging incomplete code from one ownership branch into the other every day. That defeats the separation and can create hidden dependencies. Daily pushes are required; daily cross-merges are not.

---

# 4. Directory ownership

The source package namespace below is illustrative. Replace `<verified.package>` with the agreed reverse-domain namespace before creating Java files.

```text
unircore/
├── pom.xml                         [shared; changes coordinated]
├── README.md                       [shared; coordinate API claims]
├── CONTRIBUTING.md                 [Dev 2 initial owner; shared edits]
├── CHANGELOG.md                    [Dev 2 owner]
├── LICENSE                         [owner decision; never guess]
├── .gitignore / .editorconfig      [shared bootstrap]
├── .github/workflows/ci.yml        [Dev 2 owner; shared edits coordinated]
├── src/main/java/<verified.package>/unircore/
│   ├── api/                        [Dev 1]
│   ├── core/                       [Dev 1, shared contract owner]
│   ├── orchestration/              [Dev 1]
│   ├── strategy/                   [Dev 1]
│   ├── workload/                  [Dev 1]
│   ├── algorithms/                [Dev 1 owns scheduling algorithms]
│   ├── execution/                 [Dev 2]
│   ├── runtime/                   [Dev 2; execution/runtime wiring]
│   ├── metrics/                   [Dev 2]
│   ├── diagnostics/               [Dev 2]
│   ├── spi/                        [Dev 1 defines contracts; implementations by named owner]
│   └── integrations/              [deferred; owner assigned per integration]
├── src/test/java/<verified.package>/unircore/
│   ├── api/                        [Dev 1]
│   ├── scheduling/                 [Dev 1]
│   ├── execution/                  [Dev 2]
│   ├── cache/                      [Dev 2]
│   ├── rate/                       [Dev 2]
│   ├── resilience/                 [Dev 2]
│   └── integration/                [joint, only at checkpoints]
├── examples/                       [Dev 2 primary; API examples agreed with Dev 1]
├── docs/
│   ├── architecture/               [Dev 1]
│   ├── implementation/contracts/   [joint, before each sprint]
│   ├── implementation/decisions.md [joint]
│   ├── testing/                    [Dev 2]
│   └── release/                    [Dev 2]
└── benchmarks/                     [Dev 1 scheduling; Dev 2 execution/cache; coordinated build]
```

### Ownership rules
- `pom.xml`, CI workflow, common public contracts, common exceptions, README's public usage, and build plugins are shared files. The named owner makes changes only after notifying the other developer.
- Developer 1 owns the meaning and signatures of the public API. Developer 2 owns implementation details inside `execution`, `runtime`, `metrics`, and `diagnostics`, subject to those contracts.
- Developer 1's `algorithms/` initially contains scheduling algorithms. Cache and rate-limit algorithms belong to Developer 2's feature packages if those package roots are approved; do not force all algorithms into a shared folder just because the directory is named `algorithms`.
- Avoid creating a package for future functionality until it has a scheduled deliverable.
- `integrations/` remains empty/deferred until an actual optional integration is approved.
- Test files follow production feature ownership. Integration tests are jointly owned and mainly added at sprint boundaries.
- Package layout must remain compatible with Maven's standard `src/main/java` and `src/test/java` roots.

---

# 5. Communication contracts between directories

UnirCore is a Java library, not a network service. The term “API communication” means Java interfaces and method calls between packages—not HTTP endpoints.

## 5.1 Dependency direction

```text
Consumer application
       │ calls
       ▼
api/ ─────────────── public types and stable service interfaces
       │
       ▼
orchestration/ ───── selects strategies and coordinates requests
       │
       ├── workload/ ─ validated input models and workload profiles
       ├── strategy/ ─ strategy rules and selection explanations
       ├── algorithms/ ─ scheduling algorithm implementations
       └── core/ ─ shared contracts, errors, time abstractions

execution/ ───────── task submission, queueing, lifecycle
runtime/ ─────────── wires approved implementations and defaults
metrics/ ─────────── bounded counters/timers/snapshots
diagnostics/ ─────── explanations and bounded operational state
spi/ ─────────────── extension interfaces; only where extension is required
integrations/ ────── optional framework adapters; must not leak into core
```

Keep dependencies pointing toward stable contracts. No package should depend on another package's private implementation classes. Do not make every package depend on `runtime`; runtime wiring should depend on the feature contracts and implementations, not the reverse.

## 5.2 Initial Java contracts (provisional until approved)

The following is a coordination sketch, not a mandate to implement these exact signatures without checking the R&D/API design files:

```java
public interface Scheduler {
    ScheduleResult schedule(ScheduleRequest request);
    StrategyExplanation explain(ScheduleRequest request);
}

public interface SchedulingAlgorithm {
    ScheduleResult schedule(ScheduleRequest request);
    AlgorithmId id();
}

public interface ExecutionService {
    TaskHandle submit(Callable<?> task);
    TaskInspection inspect(TaskId taskId);
    boolean cancel(TaskId taskId);
    ShutdownResult shutdown(ShutdownMode mode, Duration timeout);
}

public interface Cache<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    boolean invalidate(K key);
    CacheStats stats();
}

public interface RateLimiter {
    AcquireResult tryAcquire(int permits);
    AcquireResult acquire(int permits, Duration timeout);
}

public interface RetryExecutor {
    <T> T execute(Callable<T> operation, RetryPolicy policy);
}
```

These names may differ from the existing API design. The team must use the approved R&D/API contract as the source of truth and avoid duplicate/conflicting interfaces. In particular, avoid `Object`-heavy APIs where typed result classes are practical.

## 5.3 Component communication and ownership

| Caller / component | Calls into | Data exchanged | Owner |
|---|---|---|---|
| Consumer app | `api/` public interfaces | Immutable request/configuration, typed result/outcome | Dev 1 defines |
| `api/` scheduler facade | `orchestration/` | Validated scheduling request | Dev 1 |
| `orchestration/` | `strategy/`, `workload/`, `algorithms/` | Profile, selected algorithm, trace/result | Dev 1 |
| `execution/` | JDK executor primitives, `core/` contracts | Task, task ID/state, typed failure | Dev 2 |
| `runtime/` | API + approved implementations | Configuration and constructed service | Dev 2 implements, Dev 1 approves public surface |
| Cache feature | `core/` time/config contracts | Key/value, expiry, stats | Dev 2 |
| Rate limiter | `core/` clock/config contracts | Permit count, acquisition result | Dev 2 |
| Retry/breaker | `core/` result/error/time contracts | Policy, attempt result, breaker outcome | Dev 2 |
| Features | `metrics/` | Bounded counters/timers/events | Dev 2 |
| Consumer/example | public `api/` only | Normal use, no internal classes | Dev 2 examples |
| `integrations/` | `api/` or `spi/` only | Adapter-specific configuration | assigned later |

### Contract change procedure
1. Open a small contract-change task.
2. Describe reason, old/new signatures, compatibility impact, and affected directories.
3. Both owners agree before production code changes.
4. Update contract tests and contract document.
5. Both developers update their own implementations/fakes.
6. Integrate and run full CI at the next agreed checkpoint.

---

# 6. Sprint model and review cadence

## Sprint length
Each sprint is three working days. The sprint plan states the contract, ownership, output files, test plan, and exit criteria.

- **Day 1 of sprint:** agree contract and implement the first vertical slice.
- **Day 2:** complete main behavior and local tests.
- **Day 3:** edge cases, integration preparation, and sprint exit test.
- **Every Sunday:** weekly review, integrated build, branch synchronization, architecture/quality review, risk review, and next week's planning.

If a Sunday is a non-working day, do the review on the nearest agreed day; do not assume Sunday code work is required. The Sunday review is a scheduled review checkpoint, not a demand to work seven days every week.

## What happens at the end of each 3-day sprint
Both developers:
1. Push their own branches.
2. Freeze feature additions until integration status is known.
3. Run component tests and `mvn clean verify`.
4. Combine the agreed outputs in an integration PR.
5. Resolve interface mismatches together.
6. Run integration and regression tests.
7. Record pass/fail and deferred scope.
8. Plan the next sprint based on evidence.

## Sunday review agenda (60–90 minutes)
1. Demo each developer's working feature.
2. Review sprint acceptance criteria.
3. Run a clean build and tests from a fresh checkout.
4. Inspect public API/contract changes and dependency boundaries.
5. Review defects, flaky tests, resource limits, and benchmark results if relevant.
6. Decide merge/defer/rework for each component.
7. Update `CHANGELOG.md`, task board, risk register, and next sprint contract.
8. Assign one owner and due date to every blocker.

Do not turn the Sunday review into a requirement that one developer review every line of the other's code daily. Use the sprint checkpoint for structured integration and joint decisions.

---

# 7. Day-by-day work plan

The following plan uses 18 working days in six 3-day sprints, followed by a 3-day hardening/release sprint. Each developer has a concrete build task every day.

## Sprint 1 — Days 1–3: repository, build, and contract foundation

### Day 1 — Bootstrap in parallel
**Developer 1 (`virus`) builds:**
- Confirm artifact/group coordinates, package namespace, Java baseline, Maven, public API scope, and core package dependency rules.
- Create `api/`, `core/`, `orchestration/`, `strategy/`, `workload/`, and scheduling `algorithms/` directories.
- Draft `docs/architecture/dependency-rules.md` and first API outline.
- Add initial public contract skeletons only after naming agreement.

**Developer 2 (`zebra`) builds:**
- Create Maven build/test skeleton, JUnit configuration, `.gitignore`, `.editorconfig`, CI workflow, and test folder conventions.
- Create owned directories for `execution/`, `runtime/`, `metrics/`, `diagnostics/`, plus cache/rate/resilience test package roots as approved.
- Add smoke test and `CONTRIBUTING.md` draft.
- Verify clean build and PR workflow from a second clone.

**Shared decisions:** branch protections, license decision, directory/file ownership, `main` merge rules.

**End-of-day artifact:** project builds on both machines and CI. Empty directories may be committed only if the repository intentionally tracks them; prefer creating actual skeleton files or letting Git create folders when files arrive.

### Day 2 — Public contracts and build quality
**Developer 1 builds:**
- Define scheduler request/result, algorithm contract, strategy explanation, shared validation/error contract.
- Write public API Javadocs and contract tests for validation/default semantics.
- Record any unresolved method signature in a contract note rather than guessing.

**Developer 2 builds:**
- Configure compiler release, Surefire, test reports/coverage reporting, formatting/static checks selected by team.
- Define component test helpers and deterministic test conventions.
- Add build validation for the agreed package/source layout.

**End-of-day artifact:** contract outline and stable build conventions; no feature implementation should depend on unresolved types.

### Day 3 — First integration checkpoint
**Developer 1 builds:**
- Complete scheduling contract skeleton and sample request/result usage.
- Add scheduler contract tests and architecture dependency checks where feasible.

**Developer 2 builds:**
- Complete CI workflow and baseline build/test documentation.
- Add a minimal consumer example proving public types can be compiled from outside internal packages.

**Joint checkpoint:** clean build, test failure demonstration, API naming decision, contract freeze for Sprint 2. Open a PR to merge the bootstrap into `main` if gates pass.

**Sprint 1 exit:** both developers can build; shared contracts and file ownership are written down; CI is green.

## Sprint 2 — Days 4–6: scheduling models and baseline algorithms

### Day 4
**Developer 1 builds:** immutable scheduling/workload models; FCFS algorithm; hand-calculated FCFS tests.
**Developer 2 builds:** execution lifecycle contract draft and task-state model in owned docs/tests; do not implement against a guessed API. Add a small JDK-executor experiment/example to validate intended queue/shutdown semantics.
**Shared interface:** scheduling request/result frozen; execution contract proposal prepared for approval.

### Day 5
**Developer 1 builds:** non-preemptive SJF; waiting/turnaround/response/completion metric calculations; edge-case tests.
**Developer 2 builds:** execution contract tests using a fake/stub; define queue-full, cancellation, interruption, and shutdown behavior; add tests for those promised semantics.
**Shared interface:** `ExecutionService` and task-state behavior approved before execution implementation begins.

### Day 6 — Sprint checkpoint
**Developer 1 builds:** scheduler invariant/property tests and deterministic tie-breaking; document known scheduling semantics.
**Developer 2 builds:** execution interface skeleton plus test harness, without depending on private scheduler implementation.
**Joint checkpoint:** run all tests; verify metrics independently; integrate contract changes; open PRs and merge if green.

**Sprint 2 exit:** FCFS/SJF correct against fixtures; execution contract stable and independently testable.

## Sprint 3 — Days 7–9: scheduling orchestration + execution implementation

### Day 7
**Developer 1 builds:** priority scheduling and Round Robin if approved; validate quantum and boundary ordering.
**Developer 2 builds:** bounded execution queue and submission/completion lifecycle using suitable JDK primitives; unit tests for normal completion and failure.

### Day 8
**Developer 1 builds:** strategy-selection rules and explanation output; SRTF only if preemption contract is approved; add unsupported-case behavior.
**Developer 2 builds:** task inspection, cancellation, queue rejection, and shutdown behavior; tests for each lifecycle transition.

### Day 9 — Sprint checkpoint
**Developer 1 builds:** scheduling property tests, API/Javadocs cleanup, sample scheduler usage.
**Developer 2 builds:** concurrency tests for submissions, cancellation races, queue saturation, and shutdown; fix any flaky synchronization.
**Joint checkpoint:** full build, contract integration, consumer examples, review of scheduling and execution boundaries.

**Sprint 3 exit:** scheduler selection deterministic; execution lifecycle tests pass; no unbounded queue or silent task failure.

## Sprint 4 — Days 10–12: cache + rate limiter

### Day 10
**Developer 1 builds:** scheduler integration facade and `ScheduleResult`/trace consistency tests; add cross-checks for all supported algorithms.
**Developer 2 builds:** cache contract and bounded LRU implementation; tests for get/put/invalidate/capacity/eviction.

### Day 11
**Developer 1 builds:** workload profile/strategy-selection rules and documentation; test empty, incompatible, and invalid requests.
**Developer 2 builds:** TTL only if approved; use injectable time for expiry; add boundary and concurrent cache tests. Begin token-bucket contract if cache passes.

### Day 12 — Sprint checkpoint
**Developer 1 builds:** scheduling regression suite and benchmark baseline if correctness is stable.
**Developer 2 builds:** token-bucket rate limiter and deterministic tests for refill, burst, permits, timeout, and interruption.
**Joint checkpoint:** verify no test dependencies leak into runtime; run full suite and integrate the cache/rate contracts.

**Sprint 4 exit:** scheduling remains green; cache bounded and correct; rate-limiter semantics approved and tested to the extent implemented.

## Sprint 5 — Days 13–15: resilience and component integration

### Day 13
**Developer 1 builds:** API-level integration tests for scheduler/strategy and a consumer example; review the public error/result taxonomy.
**Developer 2 builds:** retry executor with bounded attempts, explicit retry policy, total deadline, and injectable delay/time; tests for failure preservation and interruption.

### Day 14
**Developer 1 builds:** approved SPI/extension contracts only if a real extension use case exists; otherwise add architecture boundary tests and scheduling edge cases.
**Developer 2 builds:** circuit breaker state machine (CLOSED/OPEN/HALF_OPEN) with probe limits, typed open outcome, and concurrent transition tests.

### Day 15 — Sprint checkpoint
**Developer 1 builds:** cross-component contract tests and a reviewed API consistency update if needed.
**Developer 2 builds:** retry + breaker composition tests; verify no infinite retries, retry storms, or stuck states.
**Joint checkpoint:** run concurrency suite and full CI; decide whether resource manager/load balancer stay in MVP or are deferred.

**Sprint 5 exit:** resilience behavior is bounded and state transitions tested; unapproved complexity is deferred.

## Sprint 6 — Days 16–18: metrics, diagnostics, examples, and integration

### Day 16
**Developer 1 builds:** load balancer only if its node/eligibility/update contract is approved; otherwise improve scheduling correctness and API docs.
**Developer 2 builds:** bounded metrics snapshots/counters and diagnostics; document units, cardinality, reset, and retention semantics.

### Day 17
**Developer 1 builds:** architecture dependency tests, strategy explanation tests, and public API review.
**Developer 2 builds:** consumer examples and testing utilities; ensure test-only dependencies/code do not leak into runtime artifact.

### Day 18 — Sprint checkpoint
**Developer 1 builds:** final scheduling regression tests and algorithm contract docs.
**Developer 2 builds:** compile-check all examples, finish testing docs, and verify public API usage from a separate consumer project.
**Joint checkpoint:** full integration, package/dependency review, implemented/deferred feature matrix, and Sunday review if it is the weekly boundary.

**Sprint 6 exit:** MVP feature list is explicit; examples compile; integrated build is green; no internal package access required by consumers.

## Sprint 7 — Days 19–21: hardening, performance evidence, release rehearsal

### Day 19
**Developer 1 builds:** property/randomized scheduling tests, performance baseline for stable algorithms, and a written report of algorithm limitations.
**Developer 2 builds:** execution/cache/rate/resilience stress and lifecycle tests; memory/resource-bound review and fixes.

### Day 20
**Developer 1 builds:** public API/Javadocs audit, compatibility review, final integration issue fixes.
**Developer 2 builds:** CI/release workflow checks, documentation, dependency/license inventory, source/Javadoc artifact configuration, and clean consumer build.

### Day 21 — Final review checkpoint
**Developer 1 builds:** final regression fixes and release decision notes.
**Developer 2 builds:** release checklist, changelog, quick start, examples, and publishing rehearsal plan.
**Joint checkpoint:** run `mvn clean verify` from a clean checkout; review critical defects, dependencies, examples, release metadata, and deferred scope.

**Sprint 7 exit:** release candidate only if every gate passes. Otherwise add a corrective sprint; do not release by calendar.

---

# 8. Sunday review protocol

Every Sunday (or the nearest agreed review day), perform the following in order:

1. **Demo:** each developer demonstrates their component and test output.
2. **Contract review:** confirm actual signatures and semantics still match the contract documents.
3. **Clean build:** run `mvn clean verify` from a clean checkout.
4. **Integration tests:** run component-combination and regression tests.
5. **Branch integration:** integrate only reviewed, passing work; resolve conflicts jointly.
6. **Architecture review:** check package boundaries, public/internal separation, dependencies, and resource limits.
7. **Defect review:** classify P0/P1/P2/P3; assign owner and target sprint.
8. **Scope review:** list complete, in-progress, deferred, and unsupported capabilities.
9. **Decision log:** update `docs/implementation/decisions.md` and changelog if appropriate.
10. **Next sprint:** approve next three-day contract, directory ownership, test fixtures, and exit criteria.

No Sunday review should be skipped just because “the code compiles.” Compilation does not prove correctness. Conversely, Sunday is a review checkpoint, not an expectation to work every Sunday if it is a non-working day; move the review to the nearest agreed working day.

---

# 9. Common agent instruction — applies to both developers

Use this as the shared instruction for either coding agent or AI-assisted developer.

> You are implementing UnirCore, a reusable Java library. Follow the approved R&D, public API design, architecture, validation, and testing documents as the source of truth. Do not invent requirements or claim that proposed designs are already implemented.
>
> Before coding, read the current sprint contract, directory ownership map, and the files relevant to your assigned task. Work only in your assigned directories and files. Do not edit the other developer's owned files or shared contracts without explicit agreement.
>
> Implement only the task and acceptance criteria assigned for the current day. Do not expand scope, create speculative packages, introduce frameworks, or add dependencies without approval. Reuse appropriate JDK primitives where suitable; do not reimplement established concurrency primitives without a demonstrated reason.
>
> Write implementation and meaningful tests together. Cover normal behavior, boundaries, invalid input, failure paths, and concurrency where promised. Prefer deterministic tests using injected time, controlled executors, latches, and barriers. Do not rely on arbitrary sleeps as the main synchronization technique.
>
> Preserve explicit resource bounds. Do not create infinite retries, unbounded queues, unbounded cache/diagnostic retention, or unmanaged background threads. Use monotonic time for elapsed-time/deadline logic. Keep production dependencies minimal and keep test/benchmark dependencies out of the runtime artifact.
>
> Public APIs must have clear types, documented lifecycle, error behavior, thread-safety guarantees, and limitations. Do not expose internal implementation classes or promise global optimality, universal performance, or guaranteed cancellation without evidence.
>
> Run the relevant tests and `mvn clean verify` when practical before committing. Make small coherent commits, push to your assigned branch daily, and report files changed, behavior implemented, tests run, known limitations, and blockers. Do not merge broken code to meet a date.
>
> When a contract is missing or contradictory, stop that dependent part, write a concise question/decision proposal, and use a test fake only if the interface is approved. Never silently guess a public contract.
>
> The definition of done is: implementation, tests, documentation where needed, acceptance criteria met, CI green, and changes pushed to the correct branch. Formal cross-component review occurs at sprint boundaries and Sunday checkpoints.

---

# 10. Specific agent instruction — Developer 1 (`virus`)

> You are Developer 1 and technical owner of UnirCore's public API, scheduling, and architecture boundaries.
>
> Your primary directories are `api/`, `core/`, `orchestration/`, `strategy/`, `workload/`, scheduling-related `algorithms/`, and their corresponding tests/docs. You own the meaning and stability of public API contracts. You do not own the implementation details inside Developer 2's execution/cache/rate/resilience/metrics packages.
>
> Each day, implement the assigned scheduling/API/architecture deliverable and its tests. Ensure request/result models are typed and immutable where appropriate; validation is explicit; algorithm tie-breaking and metrics are deterministic and independently testable; strategy selection is rule-based and explainable.
>
> Agree on interfaces before Developer 2 depends on them. Publish a concise contract note with signatures, invariants, errors, lifecycle behavior, and examples. Do not change shared interfaces unilaterally. If a change is necessary, open a contract-change task and coordinate both implementations.
>
> Keep package dependencies pointing toward stable contracts. Avoid making `core` a dumping ground or allowing all components to depend on runtime wiring. Reuse JDK primitives where suitable and defer features whose semantics are not clear.
>
> At each sprint boundary, deliver your implementation, tests, documentation, and a short demo. On Sunday, participate in the joint integration review, resolve contract mismatches, classify defects, and decide what can be merged. You are responsible for the final release decision but must not treat your own code as exempt from the team's quality gates.

## Developer 1 daily report format

```text
Branch: virus
Day/Sprint:
Task:
Files created/changed:
Behavior implemented:
Tests added/run:
Acceptance criteria passed/failed:
Contract changes proposed:
Known limitations:
Blockers/decisions needed:
Next day's deliverable:
```

---

# 11. Specific agent instruction — Developer 2 (`zebra`)

> You are Developer 2 and functional owner of execution, cache, rate limiting, resilience, metrics/diagnostics, and developer-facing examples/testing support.
>
> Your primary directories are `execution/`, `runtime/`, `metrics/`, `diagnostics/`, and the approved cache/rate/resilience feature packages, plus their corresponding tests, examples, CI, and testing documentation. Do not create duplicate public interfaces or change Developer 1's API contracts without coordination.
>
> Each day, implement the assigned component behavior and its tests. Prefer suitable JDK concurrency primitives over custom thread-pool machinery. Make queue, worker, retry, cache, diagnostic-retention, and resource limits explicit. Define task lifecycle, cancellation, shutdown, interruption, timeout, and expected denial outcomes clearly.
>
> Keep core runtime dependencies minimal. Do not leak JUnit/JMH or framework dependencies into production. Keep metrics bounded and avoid collecting arbitrary user payloads or secrets by default. Make time-dependent behavior testable without real sleeps.
>
> Use only approved API contracts. If a contract is unclear, write a proposed contract/test and ask for approval before building dependent production code. Do not work around a missing interface by importing another developer's private implementation.
>
> At each sprint boundary, deliver implementation, tests, docs/examples, and a short demo. On Sunday, join the integration review, report actual behavior and limitations, and help resolve integration defects. You are responsible for the reliability and lifecycle behavior of your owned components, not for checking every line of Developer 1's daily code.

## Developer 2 daily report format

```text
Branch: zebra
Day/Sprint:
Task:
Files created/changed:
Behavior implemented:
Tests added/run:
Acceptance criteria passed/failed:
Resource bounds/lifecycle verified:
Contract questions:
Known limitations:
Blockers/decisions needed:
Next day's deliverable:
```

---

# 12. Common acceptance and merge gates

## Per component
- [ ] Contract and expected behavior approved.
- [ ] Implementation is limited to assigned scope.
- [ ] Normal, boundary, invalid, and failure cases tested.
- [ ] Concurrency tests added where thread safety is promised.
- [ ] Resource bounds and lifecycle are explicit.
- [ ] Javadocs/docs/examples match behavior.
- [ ] Local build and tests pass.
- [ ] Developer's branch has been pushed.

## Per sprint
- [ ] Both developers delivered their planned artifacts.
- [ ] Shared contracts match both implementations.
- [ ] `mvn clean verify` passes.
- [ ] Component and integration tests pass.
- [ ] No unapproved dependency or package coupling.
- [ ] Unfinished features are clearly deferred.
- [ ] Joint review decision recorded.

## Before release
- [ ] A clean consumer project can compile against the built artifact.
- [ ] Javadocs, quick start, examples, version metadata, and changelog are correct.
- [ ] License and third-party dependency licenses are reviewed.
- [ ] Publishing credentials are in secure repository secrets, never source.
- [ ] Version/tag policy is followed (e.g. POM `1.0.0`, tag `v1.0.0`).
- [ ] Release checklist passes and the owner explicitly approves.
- [ ] If a mandatory gate fails, release is postponed.

---

# 13. Risk handling and decision rules

- **API disagreement:** pause dependent implementation; resolve with a contract note and update tests.
- **Directory conflict:** the owner decides implementation structure; shared contracts require joint agreement.
- **Branch drift:** integrate at sprint boundaries and Sunday; do not let branches diverge for weeks.
- **Feature behind schedule:** narrow/defer scope; do not remove tests to make the calendar.
- **Concurrency defect:** treat reproducible deadlocks, lost tasks, or unsafe state transitions as blockers.
- **Performance claim:** only publish measured results with environment and workload details.
- **Unclear component:** do not implement a vague abstraction just to fill the planned day.
- **Unstable test:** reproduce and fix or isolate with a documented reason; do not casually ignore it.
- **Release setup uncertainty:** rehearse before publishing and follow current Maven Central/Sonatype requirements.

## Final rule

Both developers build every working day. They remain independent within clear directory ownership, coordinate through stable contracts, test their own components, and integrate at three-day sprint checkpoints. Every Sunday, the team reviews the whole product and plans the next sprint. `virus` and `zebra` are ownership branches; `main` is the stable integrated product. The goal is not merely to produce code daily, but to produce working, testable increments that fit together.
