# UniCore: Unified Java Operating System Algorithms Framework

<p align="center">
  <img src="https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Testing-JUnit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" />
  <img src="https://img.shields.io/badge/Distribution-Maven%20Central-007ACC?style=for-the-badge&logo=sonatype&logoColor=white" alt="Maven Central" />
  <img src="https://img.shields.io/badge/Scope-Core%20OS%20Algorithms-blueviolet?style=for-the-badge" alt="Core OS Algorithms" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge" alt="License" />
</p>

<p align="center">
  <strong>A focused, production-grade Java library implementing foundational Operating System algorithms with clean APIs, deterministic outcomes, and clear analytical metrics.</strong>
</p>

<p align="center">
  <em>"Understand the system. Model the problem. Build the solution cleanly."</em>
</p>

---

## 📑 Table of Contents

- [Overview & Architecture Refinement](#-overview--architecture-refinement)
- [The 6 Core OS Domains](#-the-6-core-os-domains)
- [Quick Start & Code Walkthrough](#-quick-start--code-walkthrough)
  - [1. CPU Scheduling & Gantt Timeline](#1-cpu-scheduling--gantt-timeline)
  - [2. Deadlock Avoidance (Banker's Algorithm)](#2-deadlock-avoidance-bankers-algorithm)
  - [3. Dynamic Memory Partition Allocation](#3-dynamic-memory-partition-allocation)
  - [4. Virtual Memory Page Replacement](#4-virtual-memory-page-replacement)
  - [5. Secondary Storage Disk Scheduling](#5-secondary-storage-disk-scheduling)
  - [6. Classical Process Synchronization](#6-classical-process-synchronization)
- [The 40% De-scoping Decision](#-the-40-de-scoping-decision)
- [Streamlined Project Structure](#-streamlined-project-structure)
- [Documentation Index](#-documentation-index)
- [Building & Testing](#-building--testing)
- [License](#-license)

---

## 🎯 Overview & Architecture Refinement

Traditional OS simulators are often trapped in academic, legacy C codebases or clumsy desktop GUIs that cannot be easily embedded into real applications. Conversely, modern enterprise frameworks are filled with distributed cloud bloat.

**UniCore** provides a focused, modern Java 21 library delivering pure, deterministic implementations of foundational Operating System algorithms. Following a 40% architectural streamlining, UniCore removes non-OS distributed consensus, HTTP rate limiters, and RPC resilience layers, focusing 100% on core operating system fundamentals:

```text
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

## 🌐 The 6 Core OS Domains

| Domain | Key Algorithms | Core Deliverables & Outcomes |
|---|---|---|
| **1. CPU Scheduling** | FCFS, SJF, SRTF, Round Robin (Quantum), Priority (Preemptive & Non-preemptive), MLFQ | Gantt charts, Average Waiting Time, Turnaround Time, Response Time, CPU Utilization |
| **2. Process Synchronization** | Peterson's Algorithm, Mutex, Semaphores (Counting & Binary), Bounded Buffer (Producer-Consumer), Readers-Writers, Dining Philosophers | Critical section mutual exclusion, Race condition prevention, Deadlock-free IPC |
| **3. Deadlock Management** | Banker's Safety Algorithm, Resource-Request Algorithm, Resource Allocation Graph (RAG) | Safe sequence determination, Immediate request feasibility, Cycle detection |
| **4. Memory Management** | Contiguous Partitioning (First Fit, Best Fit, Worst Fit, Next Fit), Buddy System | Partition allocation, Free-list tracking, Internal & External fragmentation metrics |
| **5. Page Replacement** | FIFO, LRU, Optimal (Belady's Min), Clock / Second Chance, LFU | Page fault counts, Hit/Miss ratios, Belady's anomaly demonstration |
| **6. Disk Scheduling** | FCFS, SSTF, SCAN (Elevator), C-SCAN, LOOK, C-LOOK | Total head movement (cylinders traversed), Seek time minimization, Directional trace |

---

## 🚀 Quick Start & Code Walkthrough

### 1. CPU Scheduling & Gantt Timeline

Simulate process execution and extract exact waiting and turnaround times:

```java
import com.unicore.api.scheduler.Scheduler;
import com.unicore.model.Process;
import com.unicore.result.SchedulingResult;
import java.util.List;

List<Process> processes = List.of(
    new Process(1, "P1", /* arrival */ 0, /* burst */ 8, /* priority */ 3),
    new Process(2, "P2", /* arrival */ 1, /* burst */ 4, /* priority */ 1),
    new Process(3, "P3", /* arrival */ 2, /* burst */ 9, /* priority */ 2)
);

// Run Round Robin with Time Quantum = 2
SchedulingResult result = Scheduler.roundRobin(2).schedule(processes);

System.out.println("Average Waiting Time: " + result.getAverageWaitingTime());
System.out.println("Average Turnaround Time: " + result.getAverageTurnaroundTime());
result.getGanttChart().forEach(entry -> 
    System.out.printf("[%d - %d] %s%n", entry.startTime(), entry.endTime(), entry.processName())
);
```

---

### 2. Deadlock Avoidance (Banker's Algorithm)

Verify system safety and test whether a resource request can be safely granted:

```java
import com.unicore.algorithms.deadlock.BankersAlgorithm;
import com.unicore.result.BankersResult;

int[] available = {3, 3, 2};
int[][] max = {{7, 5, 3}, {3, 2, 2}, {9, 0, 2}, {2, 2, 2}, {4, 3, 3}};
int[][] allocation = {{0, 1, 0}, {2, 0, 0}, {3, 0, 2}, {2, 1, 1}, {0, 0, 2}};

BankersResult result = BankersAlgorithm.checkSafety(available, max, allocation);

if (result.isSafe()) {
    System.out.println("System is in a SAFE state.");
    System.out.println("Safe Sequence: " + result.getSafeSequence());
} else {
    System.out.println("DEADLOCK RISK: System is in an UNSAFE state!");
}
```

---

### 3. Dynamic Memory Partition Allocation

Evaluate allocation strategies (First Fit, Best Fit, Worst Fit) and measure fragmentation:

```java
import com.unicore.algorithms.memory.BestFitAllocator;
import com.unicore.result.MemoryAllocationResult;

int[] partitionSizes = {100, 500, 200, 300, 600};
var allocator = new BestFitAllocator(partitionSizes);

MemoryAllocationResult allocation = allocator.allocate(/* processId */ 1, /* size */ 212);
System.out.println("Allocated Block ID: " + allocation.getAllocatedBlockId());
System.out.println("Internal Fragmentation: " + allocation.getInternalFragmentation());
```

---

### 4. Virtual Memory Page Replacement

Compare page fault rates across FIFO, LRU, and Optimal algorithms:

```java
import com.unicore.algorithms.paging.LRUReplacement;
import com.unicore.result.PageReplacementResult;

int[] referenceString = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2};
int frameCount = 3;

PageReplacementResult lruResult = LRUReplacement.simulate(referenceString, frameCount);
System.out.println("Total Page Faults: " + lruResult.getPageFaultCount());
System.out.println("Hit Ratio: " + lruResult.getHitRatio() * 100 + "%");
```

---

### 5. Secondary Storage Disk Scheduling

Calculate total cylinder traversal and seek optimization:

```java
import com.unicore.algorithms.disk.SCANScheduler;
import com.unicore.result.DiskSchedulingResult;

int[] cylinderRequests = {98, 183, 37, 122, 14, 124, 65, 67};
int initialHeadPosition = 53;
int totalCylinders = 200;

DiskSchedulingResult scanResult = SCANScheduler.schedule(
    cylinderRequests, initialHeadPosition, totalCylinders, SCANScheduler.Direction.UP
);

System.out.println("Total Head Movement (Cylinders): " + scanResult.getTotalHeadMovement());
System.out.println("Seek Order: " + scanResult.getTraversalOrder());
```

---

### 6. Classical Process Synchronization

Run race-free simulations of classical IPC problems:

```java
import com.unicore.algorithms.sync.ProducerConsumerBuffer;

ProducerConsumerBuffer<String> buffer = new ProducerConsumerBuffer<>(/* capacity */ 5);
buffer.produce("OS-Data-Packet");
String item = buffer.consume();
```

---

## ✂️ The 40% De-scoping Decision

To eliminate clumsy abstractions and maintain a productive, coherent library, the following **non-OS / distributed enterprise topics were excised**:

- ❌ **Distributed Consensus:** Bully/Ring election, Lamport/Vector Clocks, Quorum consensus *(deferred to distributed systems frameworks)*.
- ❌ **Traffic Rate Limiting:** Token Bucket, Leaky Bucket, Sliding Windows *(deferred to API gateways)*.
- ❌ **Resilience Frameworks:** 3-State Circuit Breakers, Exponential Backoff Jitter *(deferred to RPC resilience libraries)*.
- ❌ **Cluster Load Balancing:** Weighted Round Robin, Consistent Hashing Ring *(deferred to reverse proxies)*.
- ❌ **Specialized Non-OS Collections:** Bloom Filters, Skip Lists, Trie *(deferred to generic DSA packages)*.
- ❌ **Clumsy 7-Layer Resolver Pipeline:** Normalizers, Strategy Resolvers, Rule Rankers *(replaced with clean, direct API calls)*.

---

## 📂 Streamlined Project Structure

```text
unicore/
├── pom.xml                                  # Java 21 LTS Maven build
├── README.md                                # Platform overview & guide
├── CONTRIBUTING.md                          # Contributor standards
├── Documentation/
│   ├── PlayBook/                            # Implementation & validation playbooks
│   ├── R&D/                                 # Architectural specifications
│   └── Study_Material/                      # 30-topic core OS concept index
└── src/
    ├── main/java/com/unicore/
    │   ├── api/                             # Clean developer facades (Scheduler, Memory, Disk)
    │   ├── model/                           # Process (PCB), MemoryBlock, DiskRequest
    │   ├── algorithms/                      # Pure OS algorithms
    │   │   ├── scheduling/                  # FCFS, SJF, SRTF, RR, Priority, MLFQ
    │   │   ├── sync/                        # Peterson, Semaphore, Producer-Consumer
    │   │   ├── deadlock/                    # Banker's algorithm, RAG cycle detector
    │   │   ├── memory/                      # First/Best/Worst Fit, Buddy System
    │   │   ├── paging/                      # FIFO, LRU, Optimal, Clock
    │   │   └── disk/                        # FCFS, SSTF, SCAN, LOOK
    │   └── result/                          # Gantt charts, fragmentation, page faults
    └── test/java/com/unicore/               # Verification tests against textbook problems
```

---

## 📚 Documentation Index

- [**Welcome & Ideology**](Documentation/PlayBook/UnirCore_Welcome_and_Ideology.md) — Mission and core OS focus.
- [**Architecture Report**](Documentation/R&D/R&D-2%20Complete_UniCore_R&D_Architecture_Report.md) — 6 core domains & direct execution pipeline.
- [**File-to-Function Plan**](Documentation/R&D/R&D-5%20File_to_Function_Level_Architecture_Plan.md) — Streamlined 44-file blueprint.
- [**Constraints & Validation**](Documentation/PlayBook/UnirCore_Constraints_Thresholds_and_Validation_Methodology.md) — Correctness constraints & validation matrix.
- [**Competitive Analysis**](Documentation/PlayBook/UnirCore_Existing_Solutions_Competitive_Analysis.md) — Comparison against legacy simulators and JDK concurrency.
- [**Parallel Implementation Plan**](Documentation/PlayBook/UnirCore_Parallel_Implementation_Ownership_Plan.md) — 2-developer sprint schedule.
- [**Study Material Syllabus**](Documentation/Study_Material/UnirCore_Study_Material_README.md) — 30-topic core OS index.

---

## 🛠️ Building & Testing

```bash
# Compile and verify with Java 21
mvn clean compile

# Run the complete test suite
mvn test

# Run validation checks
mvn verify
```

---

## 📄 License

Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
