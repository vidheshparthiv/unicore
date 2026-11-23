# UniCore — Constraints, Thresholds, and Validation Methodology

**Status:** Engineering Assurance Specification  
**Purpose:** Define parameter validation, safety invariants, and empirical validation methodology across the 6 core operating system domains.

---

## 1. Central Principle

Every algorithm parameter must be strictly classified into one of three tiers:

1. **Hard Correctness Constraints:** Mandatory mathematical invariants. Violations throw `IllegalArgumentException` immediately at the boundary.
2. **Safety Defaults:** Conservative limits to prevent infinite loops, buffer exhaustion, or simulated runaway tasks.
3. **Simulation Tuning Parameters:** Workload-dependent parameters configured by the developer (e.g., Round Robin quantum, page frame count).

---

## 2. Domain Constraint Specifications

### 2.1 CPU Scheduling
- **Time Quantum:** Must be strictly positive ($q > 0$).
- **Arrival Time:** Must be non-negative ($t_{\text{arrival}} \ge 0$).
- **Burst Time:** Must be positive ($t_{\text{burst}} > 0$).
- **Priority Values:** Explicit convention (documented whether lower number represents higher priority).
- **Invariants:** 
  $$\text{Turnaround Time} = \text{Completion Time} - \text{Arrival Time}$$
  $$\text{Waiting Time} = \text{Turnaround Time} - \text{Burst Time}$$

### 2.2 Process Synchronization
- **Semaphore Permits:** Initial permit value must be non-negative ($S \ge 0$).
- **Bounded Buffer Capacity:** Queue buffer size must be strictly positive ($N > 0$).
- **Critical Section Invariants:** Mutual Exclusion ($N_{\text{active}} \le 1$ in CS), Progress, Bounded Waiting.

### 2.3 Deadlock & Resource Allocation
- **Resource Matrix Invariants:**
  $$\text{Need}[i][j] = \text{Max}[i][j] - \text{Allocation}[i][j] \ge 0$$
  $$\sum_{i} \text{Allocation}[i][j] \le \text{TotalAvailable}[j]$$
- **Banker's Safety Check:** Must return a valid sequence $\langle P_0, P_1, \dots, P_{n-1} \rangle$ where each process can safely complete.

### 2.4 Memory Management
- **Total Memory Size:** Must be positive ($M > 0$).
- **Block Allocation Sizes:** Requested bytes/units must be positive ($s > 0$).
- **Buddy System Blocks:** Block sizes must be powers of two ($2^k$).
- **Invariants:** $\sum \text{Allocated Blocks} + \sum \text{Free Blocks} = \text{Total Memory}$.

### 2.5 Page Replacement
- **Frame Capacity:** Number of physical frames must be at least 1 ($F \ge 1$).
- **Page Numbers:** Must be non-negative integers ($p \ge 0$).
- **Invariants:** $\text{Page Faults} + \text{Page Hits} = \text{Total References}$.

### 2.6 Disk Scheduling
- **Disk Cylinders:** Current head position, track requests, and total cylinders must be non-negative ($0 \le \text{track} < \text{totalTracks}$).
- **Invariants:** Total head movement is monotonic sum of absolute cylinder displacements.

---

## 3. Validation Methodology

1. **Analytical Proof Comparison:** Compare simulation results against classic textbook benchmarks (e.g., Silberschatz & Galvin standard problem sets).
2. **Boundary Testing:** Test zero arrivals, identical burst times, reverse-sorted tracks, and saturated memory partitions.
3. **Determinism:** Given the same process list or reference string, identical outputs and Gantt charts must be reproduced 100% of the time.
