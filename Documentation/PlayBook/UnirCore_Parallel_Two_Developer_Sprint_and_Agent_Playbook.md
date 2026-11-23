# UniCore — Streamlined Two-Developer & Subagent Playbook

**Status:** Operational Playbook (Refined Scope)  
**Objective:** Maintain 100% daily engineering velocity with zero cross-blocking across the 6 core OS domains.

---

## 1. Operating Rules for Developers and AI Subagents

1. **Direct Implementations Over Framework Clumsiness:** Never wrap simple algorithm calculations in multi-tiered heuristic resolvers. A scheduler should directly implement `schedule(List<Process>) -> SchedulingResult`.
2. **Contract First:** Agree on method signatures in `com.unicore.api.*` and core models before coding algorithms.
3. **Daily Testable Output:** Every working session must yield tested algorithm code, assertions, or worked examples.
4. **Boundary Isolation:** Developer 1 works on CPU scheduling & Deadlock; Developer 2 works on Memory, Paging, Disk, and Synchronization.
5. **Deterministic Verifiability:** No arbitrary `Thread.sleep()` in tests. Use explicit step-by-step event simulation.

---

## 2. Daily Task Agenda

```
Day 1: Project setup, Process PCB model, FCFS Scheduler (Dev 1) | Memory Block model, First-Fit (Dev 2)
Day 2: SJF Scheduler, Non-preemptive Priority (Dev 1) | Best-Fit, Worst-Fit allocators (Dev 2)
Day 3: Round Robin quantum mechanics, Gantt timeline (Dev 1) | Buddy System allocator & fragmentation (Dev 2)
Day 4: SRTF Preemptive Scheduler, Waiting/TAT metrics (Dev 1) | FIFO & LRU Page Replacement (Dev 2)
Day 5: Preemptive Priority, MLFQ multi-level queues (Dev 1) | Optimal Page Replacement (Belady's) & Clock (Dev 2)
Day 6: Banker's Algorithm Safety State check (Dev 1) | Disk FCFS, SSTF Seek Calculator (Dev 2)
Day 7: Banker's Resource-Request check (Dev 1) | Disk SCAN, C-SCAN, LOOK algorithms (Dev 2)
Day 8: Resource Allocation Graph cycle detection (Dev 1) | Mutex & Counting Semaphore implementation (Dev 2)
Day 9: Peterson's Algorithm critical section model (Dev 1) | Producer-Consumer Bounded Buffer simulation (Dev 2)
Day 10: Clean UniCore fluent API facade (Dev 1) | Readers-Writers & Dining Philosophers models (Dev 2)
Day 11: End-to-end integration tests & edge cases (Both) | JMH benchmarks & Performance validation (Both)
Day 12: Release rehearsal, Maven POM validation, Documentation finalization (Both)
```
