# UnirCore — Parallel Implementation Ownership Plan

**Purpose:** Assign implementation files/components to each developer and define how both work in parallel.  
**Developer 1:** You, branch `virus`. **Developer 2:** Collaborator, branch `zebra`.  
**Target:** 21 working days across seven 3-day sprints. Extend the schedule if quality gates fail.

## 1. Ground rules

1. Both developers implement or test deliverables every working day; Developer 2 is not just a reviewer.
2. `main` remains stable. No direct feature commits to `main`.
3. `virus` is Developer 1's persistent branch; `zebra` is Developer 2's persistent branch.
4. Work within owned directories. Cross-component communication uses agreed Java interfaces and method calls, not HTTP endpoints.
5. Coordinate changes to `pom.xml`, public contracts, common exceptions, shared test utilities, and `README.md`.
6. Push daily; daily cross-merges are not required.
7. Integrate at the end of each 3-day sprint. Merge only after tests pass.
8. If a contract is missing or contradictory, agree on it before implementing dependent production code.
9. Study topics do not automatically become v1 features. Implement only approved scope.

## 2. Ownership summary

| Area | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| Primary responsibility | Public API, scheduling models/algorithms, strategy and orchestration | Execution runtime, cache, rate limiting, resilience, metrics, diagnostics, CI/examples |
| Main directories | `api/`, `core/`, `orchestration/`, `strategy/`, `workload/`, `algorithms/scheduling/` | `execution/`, `runtime/`, `algorithms/cache/`, `algorithms/rate/`, `algorithms/resilience/`, `metrics/`, `diagnostics/` |
| Tests | API, workload, scheduling, strategy, orchestration | Execution, cache, rate limiter, resilience, runtime, CI |
| Shared responsibilities | API contract decisions, integration, docs review, release readiness | Integration, shared build/CI coordination, docs review, release checklist |

## 3. Suggested repository layout

Use the package root already approved by the project. The following is a directory ownership map, not permission to invent classes.

```text
src/main/java/<approved-package>/unircore/
├── api/                         # Developer 1
├── core/                        # Developer 1; shared errors coordinated
├── orchestration/               # Developer 1
├── strategy/                    # Developer 1
├── workload/                    # Developer 1
├── algorithms/
│   ├── scheduling/              # Developer 1
│   ├── cache/                   # Developer 2
│   ├── rate/                    # Developer 2
│   └── resilience/              # Developer 2
├── execution/                   # Developer 2
├── runtime/                     # Developer 2
├── metrics/                     # Developer 2
├── diagnostics/                 # Developer 2
└── spi/                         # Shared contracts; agree before edits

src/test/java/<approved-package>/unircore/
├── api/                          # Developer 1
├── scheduling/                   # Developer 1
├── execution/                     # Developer 2
├── cache/                         # Developer 2
├── rate/                          # Developer 2
├── resilience/                   # Developer 2
└── integration/                  # Both at sprint boundaries

examples/                         # Developer 2 primary; both contribute
docs/                             # Each owns docs for assigned components
pom.xml                           # Shared; coordinate changes
.github/workflows/                # Developer 2 primary; coordinate changes
README.md                         # Shared; coordinate changes
```

## 4. Parallel 21-working-day schedule

Each sprint is three working days. Both developers work on separate deliverables every day.

### Sprint 1 — Repository, build, contracts (Days 1–3)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 1 | Review architecture/API docs; list public types and scheduling contracts; flag unresolved decisions. | Inspect build; propose CI/test baseline; map execution/runtime needs. |
| 2 | Implement approved public model/API skeletons and validation contracts. | Set up JUnit 5 structure and CI workflow; prepare test scaffolding. |
| 3 | Add API/scheduling contract tests and Javadocs. | Add build/test gates and example skeleton; document lifecycle assumptions. |
**Exit gate:** Agree Java baseline, package root, artifact coordinates, naming, and scope; `mvn clean verify` passes.

### Sprint 2 — Scheduling and execution foundations (Days 4–6)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 4 | Implement workload/job model and FCFS scheduler/simulator. | Implement bounded execution primitives against approved contracts; add tests. |
| 5 | Implement SJF and priority scheduling with deterministic tie-breaking. | Implement task lifecycle/state handling and bounded queue/rejection behavior. |
| 6 | Implement Round Robin and scheduling metrics; add worked examples. | Test completion/failure, shutdown, and resource lifecycle. |
**Exit gate:** Compare scheduling results with hand-calculated cases; test bounded queues and task states.

### Sprint 3 — Orchestration and execution integration (Days 7–9)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 7 | Implement deterministic strategy selection and capability checks. | Wire execution service to agreed task contracts. |
| 8 | Implement scheduling orchestration and configuration validation. | Add timeout/interruption handling and explicit shutdown behavior. |
| 9 | Add scheduling edge-case tests and API docs. | Add concurrency tests using latches/controlled executors, not arbitrary sleeps. |
**Exit gate:** Run integration tests; ensure no private cross-package implementation dependencies.

### Sprint 4 — Cache and rate limiting (Days 10–12)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 10 | Review cache/rate contracts; add approved integration/contract tests. | Implement bounded LRU cache and tests. |
| 11 | Add policy configuration validation and integration tests. | Implement token bucket rate limiter with testable time and boundary tests. |
| 12 | Add examples for approved policy selection; review API consistency. | Complete cache/rate concurrency, capacity tests, and docs. |
**Exit gate:** Validate capacity/eviction semantics and deterministic rate-limit decisions.

### Sprint 5 — Resilience (Days 13–15)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 13 | Agree retry/breaker public integration contracts; add contract tests. | Implement bounded retry policy, failure classification, and backoff. |
| 14 | Implement orchestration-level composition and validation. | Implement circuit-breaker state machine and transition tests. |
| 15 | Add integration tests for policy composition and error outcomes. | Test retry/breaker/timeout interactions and document caveats. |
**Exit gate:** No infinite retry loops; closed/open/half-open transitions verified.

### Sprint 6 — Metrics, diagnostics, examples (Days 16–18)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 16 | Review public API surface; add required scheduling diagnostics/metrics hooks only if approved. | Implement bounded metrics and diagnostics for approved components. |
| 17 | Write scheduling/strategy examples and public documentation. | Write execution/cache/rate/resilience examples and component documentation. |
| 18 | Run contract review and add missing scheduling/integration tests. | Run component regression tests and improve CI reporting. |
**Exit gate:** Examples compile from a clean checkout and close resources they own.

### Sprint 7 — Hardening and release rehearsal (Days 19–21)

| Day | Developer 1 — `virus` | Developer 2 — `zebra` |
|---|---|---|
| 19 | Audit public API, validation, compatibility, and unsupported claims; fix defects. | Audit bounds, concurrency, shutdown, errors, dependency scopes; fix defects. |
| 20 | Complete scheduling/boundary tests and API docs. | Complete regression tests, approved benchmarks, CI and release docs. |
| 21 | Lead release-readiness review; check version/changelog; approve or block release. | Verify artifact metadata, sources/Javadoc, publishing checklist, consumer example. |
**Exit gate:** `mvn clean verify` passes from a clean checkout; release only if all gates pass.

## 5. File-level ownership checklist

### Developer 1 — You (`virus`)

- `api/**`: public contracts, requests/results, documented guarantees.
- `core/**`: core validation and common public errors; coordinate shared changes.
- `workload/**`: workload/job models and scheduling inputs.
- `algorithms/scheduling/**`: FCFS, SJF, priority, Round Robin and scheduling metrics.
- `strategy/**`: deterministic strategy selection and capability checks.
- `orchestration/**`: composition of approved contracts.
- `src/test/java/**/api/**`
- `src/test/java/**/scheduling/**`
- Scheduling/API documentation and examples.

### Developer 2 — Collaborator (`zebra`)

- `execution/**`: task submission, results, bounded execution, task lifecycle.
- `runtime/**`: executor/resource lifecycle and shutdown.
- `algorithms/cache/**`: LRU cache, capacity and eviction behavior.
- `algorithms/rate/**`: token bucket rate limiter.
- `algorithms/resilience/**`: bounded retry/backoff and circuit breaker.
- `metrics/**` and `diagnostics/**`: only approved, bounded metrics/events.
- `src/test/java/**/execution/**`
- `src/test/java/**/cache/**`
- `src/test/java/**/rate/**`
- `src/test/java/**/resilience/**`
- `.github/workflows/**`, `examples/**`, component and release docs.

### Both developers

- `pom.xml`, public/shared interfaces, common exception types, README, integration tests, release decisions.
- Each component owner writes component tests. Both contribute integration tests at sprint boundaries.
- Never change shared API signatures or build dependencies unilaterally.

## 6. Component communication contracts

This is a Java library; these are in-process interface/method contracts, not HTTP endpoints.

| Component owner | Consumer | Contract to agree |
|---|---|---|
| `workload/` + scheduling algorithms | `strategy/`, `orchestration/` | Inputs, results/metrics, assumptions, tie-breaking. |
| `strategy/` | `orchestration/` | Strategy identity, capabilities, validated configuration. |
| `execution/` | `orchestration/` | Submission/result, rejection, cancellation, lifecycle/shutdown. |
| Cache | Approved consumers | Key/value semantics, capacity, eviction, thread-safety. |
| Rate limiter | Approved execution/orchestration | Permit decision, denial outcome, time source, validation. |
| Resilience policies | Execution/orchestration | Failure classification, attempt limits, breaker outcomes, deadline rules. |
| Metrics/diagnostics | Component owners | Bounded retention, thread safety, no sensitive payload capture. |

No component should import another component's private implementation classes.

## 7. Sprint-boundary integration procedure

At the end of Days 3, 6, 9, 12, 15, 18, and 21:

1. Freeze new feature work for review.
2. Both developers push branches and report changed files, tests, and limitations.
3. Open PRs into `main`; review shared contract changes together.
4. Run `mvn clean verify` plus relevant integration/regression tests.
5. Fix conflicts and regressions before merging.
6. Merge only reviewed, passing work. Keep `main` stable.
7. If a gate fails, extend the sprint; never bypass tests to preserve the calendar.

## 8. Daily report template

```text
Branch:
Sprint / Day:
Files created or changed:
Behavior implemented:
Tests added/run:
Build status:
Contract changes needed:
Blockers/risks:
Next deliverable:
```

## 9. Definition of done

Every component must match its contract; test success, failure, boundary, and concurrency behavior; validate configuration; bound resource use; document lifecycle/thread safety/errors; avoid unapproved dependencies; and pass tests in the integrated project.

## 10. Scope guardrails

- The 21 days are a target, not a guarantee.
- The 60-file study syllabus is not a 60-feature implementation list.
- Dining Philosophers, Banker's algorithm, page replacement, and disk scheduling are study concepts unless a concrete application-level model is approved.
- UnirCore cannot replace kernel scheduling, physical memory management, or OS disk scheduling.
- Distributed locks, leader election, virtual filesystems, and machine-learning-based “optimal” strategy selection are out of scope unless separately approved.
- Do not remove tests or quality gates to meet the target schedule.

**Ownership rule:** Developer 1 owns public API and scheduling; Developer 2 owns execution and operational algorithms. Both implement in parallel daily; formal integration/review happens at sprint boundaries.
