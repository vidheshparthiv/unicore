# UniCore — Study Material Concept Index (Streamlined)

**Folder:** `Study_Material/`  
**Purpose:** Pre-study foundational operating-system and systems-level algorithmic concepts for UniCore.  
**Scope:** Streamlined to the 30 core Operating System concepts (pruned 40% of non-OS/distributed material).

---

## Stage 1 — Process Management & CPU Scheduling

| No. | Topic | Concept |
|---:|---|---|
| 01 | `01-processes-and-threads.md` | Process Control Block (PCB), execution states, and context switching. |
| 02 | `02-cpu-scheduling-fundamentals.md` | Scheduling criteria: waiting time, turnaround time, response time, throughput. |
| 03 | `03-first-come-first-served-scheduling.md` | FCFS scheduling and the convoy effect. |
| 04 | `04-shortest-job-first-scheduling.md` | SJF (non-preemptive) algorithm and burst-time calculations. |
| 05 | `05-shortest-remaining-time-first.md` | SRTF (preemptive SJF) and dynamic preemption. |
| 06 | `06-round-robin-scheduling.md` | Round Robin scheduling, time quantum selection, and context-switch trade-offs. |
| 07 | `07-priority-scheduling-and-aging.md` | Priority scheduling, starvation, and aging mitigation. |
| 08 | `08-multilevel-queue-scheduling.md` | Multilevel queue scheduling and fixed foreground/background partitioning. |
| 09 | `09-multilevel-feedback-queue.md` | MLFQ dynamic feedback queues and quantum adjustment. |

---

## Stage 2 — Process Synchronization & Concurrency

| No. | Topic | Concept |
|---:|---|---|
| 10 | `10-critical-section-problem.md` | Mutual exclusion, progress, and bounded waiting requirements. |
| 11 | `11-petersons-algorithm.md` | Peterson's solution for two-process mutual exclusion. |
| 12 | `12-mutexes-and-semaphores.md` | Mutex locks, counting semaphores, and binary semaphores. |
| 13 | `13-producer-consumer-problem.md` | Bounded buffer synchronization with semaphores and mutex. |
| 14 | `14-readers-writers-problem.md` | Reader-preference and writer-preference synchronization. |
| 15 | `15-dining-philosophers-problem.md` | Deadlock and starvation avoidance among concurrent philosophers. |

---

## Stage 3 — Deadlock & Resource Allocation

| No. | Topic | Concept |
|---:|---|---|
| 16 | `16-deadlock-characterization.md` | The Coffman conditions: mutual exclusion, hold and wait, no preemption, circular wait. |
| 17 | `17-resource-allocation-graphs.md` | Cycle detection in resource allocation graphs (RAG). |
| 18 | `18-bankers-safety-algorithm.md` | Safe states, safe sequences, and safety validation. |
| 19 | `19-bankers-resource-request.md` | Banker's resource-request algorithm for dynamic allocation grants. |
| 20 | `20-deadlock-prevention-strategies.md` | Preventing circular wait via total ordering of resource types. |

---

## Stage 4 — Memory Management & Virtual Memory

| No. | Topic | Concept |
|---:|---|---|
| 21 | `21-contiguous-memory-allocation.md` | Fixed and dynamic partitioning, First Fit, Best Fit, Worst Fit, Next Fit. |
| 22 | `22-internal-and-external-fragmentation.md` | Fragmentation metrics and compaction concepts. |
| 23 | `23-buddy-system-allocation.md` | Binary power-of-two block allocation and coalescing. |
| 24 | `24-virtual-memory-and-paging.md` | Page tables, frames, page faults, and address translation. |
| 25 | `25-fifo-page-replacement.md` | FIFO replacement and Belady's anomaly. |
| 26 | `26-lru-page-replacement.md` | Least Recently Used replacement and stack algorithms. |
| 27 | `27-optimal-page-replacement.md` | Belady's Optimal (OPT/MIN) replacement algorithm. |
| 28 | `28-clock-and-second-chance.md` | Clock / Second Chance circular buffer replacement. |

---

## Stage 5 — Secondary Storage & Disk Scheduling

| No. | Topic | Concept |
|---:|---|---|
| 29 | `29-disk-scheduling-fcfs-and-sstf.md` | FCFS and Shortest Seek Time First (SSTF) cylinder optimization. |
| 30 | `30-disk-scheduling-scan-and-look.md` | SCAN (Elevator), C-SCAN, LOOK, and C-LOOK track traversals. |
