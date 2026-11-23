# UniCore Phase 4 — Algorithm Contracts & Strategy Selection (Streamlined)

**Status:** Technical Specification  
**Focus:** Explicit Strategy Selection across the 6 Core OS Domains  

---

## 1. Direct Selection Principle

UniCore rejects opaque or heuristic strategy selection. Developers explicitly choose the algorithm best suited for their workload or academic comparison:

```java
// Explicit selection by enum or strategy instance
SchedulingResult fcfs = scheduler.algorithm(Algorithm.FCFS).schedule(processes);
SchedulingResult sjf  = scheduler.algorithm(Algorithm.SJF).schedule(processes);
SchedulingResult rr   = scheduler.algorithm(Algorithm.ROUND_ROBIN).quantum(4).schedule(processes);
```

---

## 2. Algorithm Contracts Matrix

| Algorithm Family | Preemptive? | Asymptotic Complexity | Key Contract |
|---|---|---|---|
| **FCFS** | No | $O(n \log n)$ sort by arrival, $O(n)$ run | Executes in strict arrival order |
| **SJF** | No | $O(n \log n)$ | Selects smallest burst among ready jobs |
| **SRTF** | Yes | $O(n \log n)$ | Preempts when new arrival has shorter remaining time |
| **Round Robin** | Yes | $O(1)$ dispatch | Time-slices execution by positive quantum $q$ |
| **Priority** | Both | $O(\log n)$ queue | Orders by priority with stable tie-break |
| **Banker's Safety** | N/A | $O(m \times n^2)$ | Validates system is in safe state |
| **First Fit** | N/A | $O(k)$ partition scan | Allocates first hole of adequate size |
| **Best Fit** | N/A | $O(k)$ | Allocates smallest hole of adequate size |
| **LRU Paging** | N/A | $O(1)$ replacement | Replaces least recently referenced page |
| **Belady's OPT** | N/A | $O(F \times L)$ lookahead | Replaces page not used for longest future duration |
| **SCAN (Elevator)**| N/A | $O(r \log r)$ | Traverses tracks in one direction to end before reversing |
| **LOOK** | N/A | $O(r \log r)$ | Reverses immediately at furthest request |
