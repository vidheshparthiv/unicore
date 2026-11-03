# UniCore Algorithm Catalog

A structured catalog of the core algorithm families implemented across UniCore.

---

## 1. CPU & Task Scheduling

| Algorithm | Preemptive | Complexity | Primary Use Case | Trade-offs |
|---|---|---|---|---|
| **FCFS** (First-Come, First-Served) | No | $O(n \log n)$ or $O(1)$ | Simple batch queues | Susceptible to convoy effect |
| **SJF** (Shortest Job First) | No | $O(n \log n)$ | Batch workloads with known bursts | Optimal wait time; risk of starvation for long jobs |
| **SRTF** (Shortest Remaining Time First) | Yes | $O(n \log n)$ | Interactive task systems | High context switching; requires accurate estimates |
| **Round Robin** (RR) | Yes | $O(1)$ dispatch | Time-shared task execution | Fair; performance heavily dependent on time quantum |
| **Priority Scheduling** | Both | $O(\log n)$ | Real-time & prioritized work | Starvation of low-priority tasks unless aging is applied |
| **MLFQ** (Multilevel Feedback Queue) | Yes | $O(1)$ dispatch | Complex mixed workloads | Automatically penalizes CPU-bound jobs; complex configuration |

---

## 2. In-Memory Cache Eviction

| Algorithm | Complexity | Eviction Strategy | Memory Overhead |
|---|---|---|---|
| **LRU** (Least Recently Used) | $O(1)$ | Evicts least recently accessed entry | Moderate (doubly linked list nodes) |
| **FIFO** (First-In, First-Out) | $O(1)$ | Evicts oldest inserted entry | Low (simple queue order) |
| **Clock / Second Chance** | $O(1)$ amortized | Approximates LRU via reference bits | Minimal (circular buffer + single bit) |
| **LFU** (Least Frequently Used) | $O(1)$ | Evicts least frequently accessed entry | Higher (frequency buckets + access lists) |

---

## 3. Traffic Management & Rate Limiting

| Algorithm | Burst Allowance | Smoothing | Complexity |
|---|---|---|---|
| **Token Bucket** | Configurable burst capacity | Moderate | $O(1)$ time calculation |
| **Leaky Bucket** | Strictly bounded rate | High (smooth output stream) | $O(1)$ or $O(\text{queue length})$ |
| **Sliding Window Counter** | Low boundary burst | Good | $O(1)$ memory, low computation |

---

## 4. Resilience & Fault Tolerance

| Pattern | Mechanism | State Space |
|---|---|---|
| **Exponential Backoff with Jitter** | $t_i = \min(t_{\max}, t_{\text{base}} \cdot 2^i) \cdot \text{jitter}$ | Stateless / Attempt-based |
| **3-State Circuit Breaker** | Tracks failure rates across sliding window | `CLOSED` $\leftrightarrow$ `OPEN` $\leftrightarrow$ `HALF_OPEN` |

---

## 5. Deadlock & Resource Management

| Algorithm | Purpose | Invariants |
|---|---|---|
| **Banker's Algorithm** | Safe state verification before resource allocation | $\text{Need} \le \text{Available}$; detects circular-wait conditions |
| **Resource Allocation Graph (RAG)** | Cycle detection for multi-instance resources | Graph traversal; checks for deadlock existence |
