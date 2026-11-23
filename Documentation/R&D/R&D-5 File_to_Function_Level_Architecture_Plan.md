# UniCore — Technical Function-Level Architecture Plan (Streamlined)

**Status:** Technical Design Specification  
**Scope:** Streamlined ~35-file blueprint focusing strictly on the 6 Core OS Domains (40% non-OS complexity removed).

---

## 1. Architectural Flow: Direct & Clean

Instead of passing through 7 intermediate layers, requests invoke algorithm engines directly:

```text
Application ──► UniCore Facade / Specific Engine ──► Pure Algorithm ──► Typed Outcome Result
```

---

## 2. File-by-File Blueprint

### A. Root & Build Configuration
- **F01: `pom.xml`** — Maven coordinates `com.unicore:unicore`, Java 21, JUnit 5, JMH.
- **F02: `README.md`** — Overview, quick start, domain breakdown, and usage examples.
- **F03: `LICENSE` & `NOTICE`** — Apache 2.0 licensing.

### B. Domain Models (`com.unicore.model.*`)
- **F04: `Process.java`** — Process Control Block (PCB): `pid`, `name`, `arrivalTime`, `burstTime`, `priority`, `remainingTime`, and `ProcessState` enum (`NEW`, `READY`, `RUNNING`, `WAITING`, `TERMINATED`).
- **F05: `MemoryBlock.java`** — Memory partition descriptor: `blockId`, `startAddress`, `size`, `isFree`, `allocatedProcessId`.
- **F06: `PageReference.java`** — Virtual memory page access: `pageNumber`, `accessTime`.
- **F07: `DiskRequest.java`** — Disk cylinder request: `cylinderNumber`, `arrivalTime`.

### C. Public APIs & Builders (`com.unicore.api.*`)
- **F08: `SchedulerBuilder.java`** — Fluent builder for CPU schedulers (FCFS, SJF, SRTF, RR, Priority, MLFQ).
- **F09: `MemoryManagerBuilder.java`** — Builder for memory partition allocators and Buddy System.
- **F10: `PageReplacementBuilder.java`** — Builder for FIFO, LRU, Optimal, Clock page replacers.
- **F11: `DiskSchedulerBuilder.java`** — Builder for disk seek optimizers.
- **F12: `SyncPrimitives.java`** — Factory for Peterson's lock, counting semaphores, and classic IPC scenarios.

### D. CPU Scheduling Subsystem (`com.unicore.algorithms.scheduling.*`)
- **F13: `SchedulingAlgorithm.java`** — Core interface: `SchedulingResult schedule(List<Process> processes)`.
- **F14: `FCFSScheduler.java`** — First-Come, First-Served non-preemptive algorithm.
- **F15: `SJFScheduler.java`** — Shortest Job First non-preemptive algorithm.
- **F16: `SRTFScheduler.java`** — Shortest Remaining Time First (preemptive SJF).
- **F17: `RoundRobinScheduler.java`** — Round Robin with configurable time quantum $q$.
- **F18: `PriorityScheduler.java`** — Priority scheduling (both preemptive and non-preemptive modes).
- **F19: `MLFQScheduler.java`** — Multilevel Feedback Queue with quantum degradation.

### E. Process Synchronization Subsystem (`com.unicore.algorithms.sync.*`)
- **F20: `PetersonLock.java`** — Classical two-process critical section solution.
- **F21: `CountingSemaphore.java`** — $P()$ / `wait()` and $V()$ / `signal()` semaphore primitive.
- **F22: `ProducerConsumerBuffer.java`** — Bounded-buffer simulation with empty/full semaphores and mutex.
- **F23: `ReadersWritersSolution.java`** — Reader-priority and writer-priority synchronization models.
- **F24: `DiningPhilosophers.java`** — Asymmetric and resource-hierarchy deadlock-free solutions.

### F. Deadlock Management Subsystem (`com.unicore.algorithms.deadlock.*`)
- **F25: `BankersAlgorithm.java`** — Banker's safety state algorithm and resource-request algorithm.
- **F26: `ResourceAllocationGraph.java`** — Directed graph cycle detection for deadlocks.

### G. Memory Management Subsystem (`com.unicore.algorithms.memory.*`)
- **F27: `MemoryAllocator.java`** — Interface: `allocate(int size, int processId)`, `deallocate(int processId)`.
- **F28: `FirstFitAllocator.java`** — Allocates first hole large enough.
- **F29: `BestFitAllocator.java`** — Allocates smallest hole that fits to minimize leftover space.
- **F30: `WorstFitAllocator.java`** — Allocates largest available hole.
- **F31: `BuddySystemAllocator.java`** — Power-of-two recursive splitting and coalescing allocator.

### H. Virtual Memory & Page Replacement Subsystem (`com.unicore.algorithms.paging.*`)
- **F32: `PageReplacementAlgorithm.java`** — Interface: `PageReplacementResult execute(int[] referenceString, int frameCount)`.
- **F33: `FIFOReplacement.java`** — First-In, First-Out replacement.
- **F34: `LRUReplacement.java`** — Least Recently Used replacement.
- **F35: `OptimalReplacement.java`** — Belady's Optimal Min replacement.
- **F36: `ClockReplacement.java`** — Second Chance / Clock reference-bit replacement.

### I. Disk Scheduling Subsystem (`com.unicore.algorithms.disk.*`)
- **F37: `DiskScheduler.java`** — Interface: `DiskSchedulingResult schedule(int[] requests, int initialHeadPosition)`.
- **F38: `SSTFScheduler.java`** — Shortest Seek Time First.
- **F39: `SCANScheduler.java`** — SCAN (Elevator algorithm) and C-SCAN.
- **F40: `LOOKScheduler.java`** — LOOK and C-LOOK seek optimization.

### J. Output Results & Metrics (`com.unicore.result.*`)
- **F41: `SchedulingResult.java`** — Gantt chart, waiting times, turnaround times, completion times.
- **F42: `MemoryAllocationResult.java`** — Allocated segments, free list, internal & external fragmentation.
- **F43: `PageReplacementResult.java`** — Page faults, hits, hit ratio, frame states.
- **F44: `DiskSchedulingResult.java`** — Total cylinder displacement, traversal sequence, average seek time.

---

## 3. Benefits of the Streamlined Architecture

1. **40% Fewer Source Files:** Drops from 96 clumsy files to 44 cleanly organized, purpose-driven files.
2. **Zero Middleware Clutter:** Removes rate-limiters, circuit breakers, and distributed consensus.
3. **Immediate Developer Value:** Every class produces clear, tangible, and textbook-verifiable OS metrics.
