# UniCore Phase 1 — Public API and Class Design (Streamlined)

**Status:** Technical API Specification  
**Focus:** Clean, Ergonomic Public Surface for the 6 Core Operating System Domains  

---

## 1. Consumer Design Philosophy

The developer API must be **fluent, obvious, and immediate**. A developer should not need to configure complex resolvers or capability registries to run an algorithm.

```java
// Example 1: CPU Scheduling Simulation
SchedulingResult result = UniCore.scheduler()
    .algorithm(Algorithms.ROUND_ROBIN)
    .quantum(2)
    .schedule(processList);

// Example 2: Deadlock Safety Check (Banker's Algorithm)
BankersResult result = UniCore.deadlock()
    .bankers()
    .available(availableVector)
    .maximum(maxMatrix)
    .allocation(allocationMatrix)
    .checkSafety();

// Example 3: Page Replacement Analysis
PageResult pageResult = UniCore.paging()
    .lru()
    .frames(3)
    .simulate(new int[]{7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2});

// Example 4: Disk Scheduling Optimization
DiskResult diskResult = UniCore.disk()
    .scan()
    .initialHead(53)
    .direction(Direction.UP)
    .schedule(new int[]{98, 183, 37, 122, 14, 124, 65, 67});
```

---

## 2. Public API Surface

### 2.1 `com.unicore.api.scheduler`
- `SchedulerBuilder`: Configures algorithm (FCFS, SJF, SRTF, RR, Priority, MLFQ), preemption mode, quantum.
- `SchedulingAlgorithm`: Standard contract accepting a process list and returning `SchedulingResult`.

### 2.2 `com.unicore.api.sync`
- `SyncFactory`: Instantiates `PetersonLock`, `CountingSemaphore`, `ProducerConsumerBuffer`, `ReadersWritersSolution`, and `DiningPhilosophers`.

### 2.3 `com.unicore.api.deadlock`
- `BankersAlgorithm`: Computes safe states and checks resource-request viability.
- `ResourceAllocationGraph`: Detects cycles in multi-instance resource systems.

### 2.4 `com.unicore.api.memory`
- `MemoryManager`: Provides `allocate(size, pid)` and `deallocate(pid)` using First Fit, Best Fit, Worst Fit, Next Fit, or Buddy System.

### 2.5 `com.unicore.api.paging`
- `PageReplacer`: Evaluates FIFO, LRU, Optimal, and Clock algorithms, producing hit/fault statistics.

### 2.6 `com.unicore.api.disk`
- `DiskScheduler`: Computes total cylinder head movements for FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK.
