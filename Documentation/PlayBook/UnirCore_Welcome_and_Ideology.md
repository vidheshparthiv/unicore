# Welcome to UniCore

**UniCore** is a focused, production-grade Java systems library dedicated to making foundational **Operating System algorithms** understandable, verifiable, and directly reusable in Java applications and simulations.

---

## 🎯 Our Refined Ideology

**Understand the system. Model the problem. Build the solution cleanly.**

Following our architectural streamlining (reducing non-OS complexity by 40%), UniCore adheres to core principles:

- **Core OS First:** Focus 100% on foundational operating system mechanisms: CPU scheduling, process synchronization, deadlock avoidance, memory allocation, page replacement, and disk scheduling.
- **Simplicity Over Clumsiness:** Reject artificial multi-layer indirection. Replace 7-layer resolvers and complex strategy pipelines with direct, clean, and productive APIs.
- **Evidence Over Claims:** Verify every algorithm using hand-calculated mathematical test vectors and deterministic JUnit 5 test suites.
- **Pure & Deterministic:** Keep algorithm mechanics isolated, reproducible, and free from external infrastructure baggage.
- **Zero Framework Lock-in:** Pure Java 21 LTS with zero mandatory third-party runtime dependencies.

---

## 🧱 The 6 Core OS Domains (Focused Scope)

UniCore concentrates strictly on the 6 essential operating systems domains:

1. **CPU Scheduling:** FCFS, SJF, SRTF, Round Robin, Priority (Preemptive & Non-preemptive), and Multilevel Feedback Queue (MLFQ).
2. **Process Synchronization:** Critical Section (Peterson's), Mutex, Binary/Counting Semaphores, Producer-Consumer, Readers-Writers, Dining Philosophers.
3. **Deadlock Management:** Banker's Algorithm (Safety & Resource-Request), Resource Allocation Graph (RAG) cycle detection.
4. **Memory Management:** Contiguous Allocation (First Fit, Best Fit, Worst Fit, Next Fit), Buddy System Allocation, Fragmentation tracking.
5. **Virtual Memory & Page Replacement:** FIFO, LRU, Optimal (Belady's), Clock / Second Chance, LFU, and Page Fault analysis.
6. **Disk Scheduling:** FCFS, SSTF, SCAN (Elevator), C-SCAN, LOOK, C-LOOK seek optimization.

*(Note: Advanced distributed consensus, enterprise rate limiting, circuit breakers, and external load balancers have been pruned to keep UniCore simple, focused, and productive.)*

---

## 🚀 Our Goal

To provide students, engineers, and researchers with a reliable, elegant, and high-performance Java library that implements classical operating system algorithms with absolute correctness, clear telemetry, and minimal friction.
