# UniCore — R&D Architecture and Product Definition Report (Streamlined)

## Unified Java Operating System Algorithms & Simulation Framework

---

## 1. Product Definition & Vision

**UniCore** is a focused, production-grade Java library that provides clean, modern, and verifiable implementations of fundamental **Operating System algorithms**. 

Rather than bloating into microservice rate limiters, distributed consensus engines, or complex cloud resilience mechanisms (which have been intentionally removed to reduce project complexity by 40%), UniCore concentrates exclusively on core systems algorithms that form the bedrock of computer science and OS engineering.

Distributed via **Maven Central** (`com.unicore:unicore`), UniCore enables developers, researchers, and educators to embed, benchmark, and simulate classical OS algorithms within standard Java 21+ applications.

---

## 2. Streamlined Architectural Flow

UniCore replaces clumsy multi-layer indirection (normalizers, analyzers, strategy resolvers, capability descriptors) with a direct, clean, and productive 3-stage execution pipeline:

```
┌────────────────────────────────────────────────────────┐
│                   Consumer Application                 │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   UniCore Public API                   │
│   (Schedulers, Sync Primitives, Banker's, Memory,      │
│               Paging, Disk Schedulers)                 │
└───────────────────────────┬────────────────────────────┘
                            │ (Direct Invocation)
                            ▼
┌────────────────────────────────────────────────────────┐
│               Core OS Algorithm Engines                │
│    FCFS • SJF • SRTF • RR • Priority • MLFQ            │
│    Peterson • Semaphores • Producer-Consumer           │
│    Banker's Algorithm • RAG Cycle Detection            │
│    First/Best/Worst Fit • Buddy Allocator              │
│    FIFO • LRU • Optimal • Clock • LFU                  │
│    FCFS • SSTF • SCAN • C-SCAN • LOOK • C-LOOK         │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│             Typed Results & Metrics Output             │
│    Gantt Charts • Waiting/Turnaround Times • Safe Seq  │
│    Fragmentation Stats • Page Faults • Seek Displacements
└────────────────────────────────────────────────────────┘
```

---

## 3. The 6 Core OS Domains (The Retained 60%)

UniCore organizes its capabilities into 6 core OS domains:

| # | OS Domain | Primary Algorithms / Models | Key Outputs & Outcomes |
|---|---|---|---|
| **1** | **CPU Scheduling & Processes** | FCFS, SJF, SRTF, Round Robin, Priority (Preemptive & Non-preemptive), MLFQ | Gantt timeline, Waiting Time, Turnaround Time, Response Time, CPU Utilization |
| **2** | **Process Synchronization** | Peterson's Algorithm, Mutex, Semaphores (Counting & Binary), Bounded Buffer (Producer-Consumer), Readers-Writers, Dining Philosophers | Race condition prevention, Mutual exclusion, Deadlock-free concurrency |
| **3** | **Deadlock Management** | Banker's Safety Algorithm, Resource-Request Algorithm, Resource Allocation Graph (RAG) Cycle Detection | Safe execution sequence, Immediate request safety verification, Deadlock cycle detection |
| **4** | **Memory Management** | Dynamic Contiguous Partition Allocation (First Fit, Best Fit, Worst Fit, Next Fit), Buddy System | Allocated blocks, Free blocks, Internal & External fragmentation analysis |
| **5** | **Virtual Memory & Paging** | FIFO, LRU, Optimal (Belady's Min), Clock / Second Chance, LFU | Page fault count, Hit/Miss ratio, Frame state visualization |
| **6** | **Disk / Storage Scheduling** | FCFS, SSTF, SCAN (Elevator), C-SCAN, LOOK, C-LOOK | Total head movement (cylinders), Average seek time, Traversal path trace |

---

## 4. The 40% De-scoping & Simplification (What Was Removed)

To ensure the library is simple, maintainable, and high-yielding, the following advanced/non-OS topics were excised:

1. **Distributed Consensus & Coordination (Removed):** Bully Election, Ring Election, Vector Clocks, Lamport Timestamps, Quorum consensus. *(Out of scope; belongs to distributed database frameworks like Raft/Paxos).*
2. **Traffic Shaping & Rate Limiting (Removed):** Token Bucket, Leaky Bucket, Sliding Windows. *(Belongs to API gateways and web proxy infrastructure).*
3. **Resilience & Fault Tolerance (Removed):** 3-State Circuit Breakers, Retry with Exponential Backoff and Jitter. *(Belongs to microservice RPC libraries like Resilience4j).*
4. **Cluster Load Balancing (Removed):** Weighted Round Robin, Least Connections, Consistent Hashing Ring. *(Belongs to reverse proxy layers like NGINX/Envoy).*
5. **Specialized Systems Collections (Removed):** Bloom Filters, Skip Lists, Trie, B/B+ Trees, Disruptor RingBuffer. *(General advanced DSA, distracting from pure OS mechanics).*
6. **Clumsy 7-Layer Indirection (Removed):** Workload Profilers, Heuristic Strategy Resolvers, Capability Descriptors, Decision Tracers. *(Replaced by clean, direct algorithm calls).*

---

## 5. Clean Repository Package Hierarchy

The source code layout is simplified from 140 sprawling directories down to a clean, cohesive package structure:

```text
com.unicore/
├── api/                             # Clean, user-facing facade and fluent builders
│   ├── scheduler/                   # Scheduler builder & interfaces
│   ├── sync/                        # Synchronization & classic problem interfaces
│   ├── deadlock/                    # Deadlock avoidance interfaces
│   ├── memory/                      # Memory manager interfaces
│   ├── paging/                      # Page replacement interfaces
│   └── disk/                        # Disk scheduler interfaces
├── model/                           # Domain entities
│   ├── Process.java                 # PCB model (state, burst, arrival, priority)
│   ├── MemoryBlock.java             # Memory partition block
│   ├── PageReference.java           # Virtual memory reference
│   └── DiskRequest.java             # Track/cylinder request
├── algorithms/                      # Pure, deterministic algorithm implementations
│   ├── scheduling/                  # FCFS, SJF, SRTF, RR, Priority, MLFQ
│   ├── sync/                        # Peterson, Semaphore, Mutex, IPC problems
│   ├── deadlock/                    # Banker's algorithm, RAG cycle detector
│   ├── memory/                      # FirstFit, BestFit, WorstFit, NextFit, Buddy
│   ├── paging/                      # FIFO, LRU, Optimal, Clock, LFU
│   └── disk/                        # FCFS, SSTF, SCAN, CSCAN, LOOK, CLOOK
└── result/                          # Standardized outcome structures
    ├── SchedulingResult.java        # Gantt chart, waiting/turnaround metrics
    ├── MemoryAllocationResult.java  # Partition allocations, fragmentation stats
    ├── PageReplacementResult.java   # Fault counts, hit/miss ratios
    └── DiskSchedulingResult.java    # Cylinder seek counts, head traversal path
```
