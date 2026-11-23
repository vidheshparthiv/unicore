# UniCore Phase 2 — Internal Architecture & Module Boundaries (Streamlined)

**Status:** Technical Architecture Specification  
**Focus:** Package Decoupling and Clean Flow for the 6 Core OS Domains  

---

## 1. Clean Package Architecture

UniCore organizes its codebase into three crisp layers:

```text
┌────────────────────────────────────────────────────────┐
│  1. API Layer (com.unicore.api.*)                      │
│     Fluent Builders • High-level Facades               │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  2. Domain Models (com.unicore.model.*)                │
│     Process (PCB) • MemoryBlock • PageReference • Track│
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  3. Pure Algorithm Engine (com.unicore.algorithms.*)   │
│     Scheduling • Sync • Deadlock • Memory • Paging •   │
│                      Disk Seek                         │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│  4. Results & Metrics (com.unicore.result.*)           │
│     Gantt Timelines • Fragmentation • Fault Ratios     │
└────────────────────────────────────────────────────────┘
```

---

## 2. Invariants & Module Rules

1. **Pure Algorithmic Independence:** Algorithms never call API builders or UI formats. An algorithm receives clean domain data and returns a pure result object.
2. **Model Immutability:** Output results and Gantt timeline slices are immutable records.
3. **No Circular Dependencies:** Package dependencies flow strictly downward (`api` $\to$ `algorithms` $\to$ `model` / `result`).
4. **Zero Overhead:** No reflection, dynamic byte code generation, or heavy dependency injection containers. Pure, high-speed Java 21 LTS code.
