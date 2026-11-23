# UniCore — Streamlined Parallel Implementation Ownership Plan

**Purpose:** Streamlined 2-developer ownership plan for the 6 core OS domains.  
**Developer 1:** Project Owner (Branch `virus`)  
**Developer 2:** Collaborator (Branch `zebra`)  
**Scope:** Core Operating System Algorithms (40% non-OS bloat removed).

---

## 1. Ownership Matrix

| Domain | Developer 1 (`virus`) | Developer 2 (`zebra`) |
|---|---|---|
| **CPU Scheduling & Processes** | **Lead Owner:** Process model (PCB), FCFS, SJF, SRTF, Round Robin, Priority, MLFQ, Gantt chart generator | Secondary review & edge-case test contributor |
| **Deadlock & Resources** | **Lead Owner:** Banker's Algorithm (Safety & Request checks), Resource Allocation Graph (RAG) | Secondary review |
| **Memory Management** | Secondary review & integration | **Lead Owner:** Dynamic partition allocators (First Fit, Best Fit, Worst Fit, Next Fit), Buddy System, fragmentation metrics |
| **Page Replacement** | Secondary review & integration | **Lead Owner:** FIFO, LRU, Optimal (Belady), Clock, LFU, page fault calculators |
| **Disk Scheduling** | Secondary review & integration | **Lead Owner:** FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK seek calculators |
| **Process Synchronization** | Critical Section & Peterson's algorithm | **Lead Owner:** Mutex, Semaphores, Producer-Consumer, Readers-Writers, Dining Philosophers |
| **Shared Infrastructure** | Public API façade, shared models, Maven `pom.xml` | GitHub Actions CI, test kit utilities, benchmark harness |

---

## 2. Streamlined Sprint Schedule (12 Working Days)

### Sprint 1: Core Models & Foundation (Days 1–3)
- **Dev 1:** Process model (PCB), state transitions, FCFS and SJF schedulers.
- **Dev 2:** Memory partition models, First Fit and Best Fit allocation engines.
- **Exit Gate:** `mvn test` passes with verified textbook calculations.

### Sprint 2: Preemptive Scheduling & Memory Subsystems (Days 4–6)
- **Dev 1:** Round Robin with quantum, Priority scheduling, SRTF, and Gantt charts.
- **Dev 2:** Worst Fit, Next Fit, Buddy System allocator, and fragmentation metrics.
- **Exit Gate:** Preemption simulation verified against reference timelines.

### Sprint 3: Deadlock & Virtual Memory (Days 7–9)
- **Dev 1:** Banker's algorithm (safety check, request grant), RAG cycle detection.
- **Dev 2:** Page replacement algorithms (FIFO, LRU, Optimal, Clock) and page fault stats.
- **Exit Gate:** Banker's safe sequence tests; Belady's anomaly tests verified.

### Sprint 4: Synchronization, Disk I/O & Final Polish (Days 10–12)
- **Dev 1:** MLFQ scheduler, Peterson's algorithm, integrated public API.
- **Dev 2:** Disk scheduling (SSTF, SCAN, LOOK), semaphores, IPC simulations (Producer-Consumer).
- **Exit Gate:** Full test suite passes; complete examples and documentation ready.
