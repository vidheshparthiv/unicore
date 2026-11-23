# UniCore Phase 3 — Correctness, Validation & Error Handling (Streamlined)

**Status:** Technical Error Handling Specification  
**Focus:** Explicit Invariant Enforcement across Core OS Domains  

---

## 1. Exception Hierarchy

UniCore utilizes a clear, focused exception model rooted in standard Java conventions:

```text
java.lang.RuntimeException
    └── UniCoreException (Base unchecked exception)
            ├── InvalidProcessException (Malformed PCB, negative burst/arrival)
            ├── InsufficientResourcesException (Banker's deadlock danger or allocation denial)
            ├── MemoryOverflowException (No contiguous hole fits or pool exhausted)
            ├── InvalidFrameCountException (Frame capacity <= 0)
            └── InvalidTrackException (Negative or out-of-bounds cylinder request)
```

---

## 2. Invariant Rules by Domain

| Domain | Invariant Rule | Violation Outcome |
|---|---|---|
| **CPU Scheduling** | Burst time must be $> 0$; Arrival time must be $\ge 0$; Quantum $> 0$ | Throws `InvalidProcessException` |
| **Process Sync** | Initial semaphore count $\ge 0$; buffer size $> 0$ | Throws `IllegalArgumentException` |
| **Deadlock Management** | $\text{Need} = \text{Max} - \text{Allocated} \ge 0$; $\text{Request} \le \text{Need}$ | Throws `InsufficientResourcesException` |
| **Memory Allocation** | Requested block size $> 0$; deallocation of unowned block rejected | Throws `MemoryOverflowException` / `IllegalArgumentException` |
| **Page Replacement** | Frame capacity $\ge 1$; page references $\ge 0$ | Throws `InvalidFrameCountException` |
| **Disk Scheduling** | Cylinder track $\ge 0$ and $< \text{maxCylinders}$ | Throws `InvalidTrackException` |

---

## 3. Boundary Validation Strategy

- **Fail-Fast:** All inputs are inspected immediately at the entry point of the algorithm before any state mutation or simulation begins.
- **Clear Diagnostic Messages:** Error messages explicitly report the offending process, negative value, or violated matrix constraint.
