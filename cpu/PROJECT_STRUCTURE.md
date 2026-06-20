# CPU Scheduling & OS Simulator Library

## Project Overview

A comprehensive Java library for simulating CPU scheduling algorithms and OS operations. Built with Java 21, this library provides realistic simulations of various CPU scheduling strategies used in operating systems.

---
## 📁 Project Structure

```
cpu/
├── src/
│   ├── main/java/com/unicore/scheduler/
│   │   ├── core/
│   │   │   ├── Process.java              # Process model with state management
│   │   │   ├── SchedulingAlgorithm.java  # Interface for scheduling strategies
│   │   │   ├── SchedulingResult.java     # Results and metrics
│   │   │   ├── ProcessQueue.java         # Queue management
│   │   │   └── Scheduler.java            # Main scheduler controller
│   │   │
│   │   ├── algorithms/
│   │   │   ├── FCFSScheduler.java                    # First Come First Served
│   │   │   ├── SJFScheduler.java                     # Shortest Job First
│   │   │   ├── RoundRobinScheduler.java              # Round Robin with quantum
│   │   │   ├── PriorityScheduler.java                # Non-preemptive Priority
│   │   │   └── PreemptivePriorityScheduler.java      # Preemptive Priority
│   │   │
│   │   ├── memory/
│   │   │   └── MemoryManager.java        # Memory allocation and management
│   │   │
│   │   ├── utils/
│   │   │   └── SimulationLogger.java     # Logging and reporting utilities
│   │   │
│   │   └── SchedulerDemo.java            # Demo application
│   │
│   ├── resources/
│   │   └── application.properties        # Configuration
│   │
│   └── test/java/com/unicore/scheduler/
│       └── SchedulerTest.java            # Comprehensive test suite
│
├── pom.xml                               # Maven configuration
├── mvnw                                  # Maven wrapper (Unix)
├── mvnw.cmd                              # Maven wrapper (Windows)
└── README.md                             # This file
```

---

## 🎯 Core Components

### 1. **Process Model** (`core/Process.java`)
- Represents a process with scheduling parameters
- Tracks process state (NEW, READY, RUNNING, WAITING, COMPLETED)
- Maintains metrics: arrival time, burst time, priority
- Calculates: waiting time, turnaround time, completion time

### 2. **SchedulingAlgorithm Interface** (`core/SchedulingAlgorithm.java`)
- Defines contract for all scheduling implementations
- Methods: `schedule()`, `getName()`, `getDescription()`
- Extensible for adding new algorithms

### 3. **SchedulingResult** (`core/SchedulingResult.java`)
- Contains execution timeline (Gantt chart)
- Calculates performance metrics:
  - Average waiting time
  - Average turnaround time
  - Total completion time
- Provides formatted output for analysis

### 4. **Scheduler** (`core/Scheduler.java`)
- Central controller for scheduling
- Manages process queues
- Tracks system time
- Coordinates with scheduling algorithms

---

## 📊 Scheduling Algorithms

### 1. **FCFS (First Come First Served)**
- **Type:** Non-preemptive
- **Strategy:** Processes execute in arrival order
- **Pros:** Simple, fair
- **Cons:** High average waiting time
- **Best for:** Interactive systems with deadline requirements

### 2. **SJF (Shortest Job First)**
- **Type:** Non-preemptive
- **Strategy:** Selects process with shortest burst time
- **Pros:** Minimizes average waiting time
- **Cons:** Starvation of long processes
- **Best for:** Batch processing

### 3. **Round Robin (RR)**
- **Type:** Preemptive
- **Strategy:** Each process gets time quantum (default: 4)
- **Pros:** Fair, good for interactive systems
- **Cons:** More context switches
- **Best for:** Time-sharing systems

### 4. **Priority Scheduling (Non-Preemptive)**
- **Type:** Non-preemptive
- **Strategy:** Processes execute by priority (lower number = higher priority)
- **Pros:** Handles real-time requirements
- **Cons:** Starvation possible
- **Best for:** Real-time systems

### 5. **Preemptive Priority**
- **Type:** Preemptive
- **Strategy:** Higher priority process preempts lower priority
- **Pros:** Better response time for high priority
- **Cons:** More context switches
- **Best for:** Real-time systems with strict requirements

---

## 💾 Memory Management

### MemoryManager (`memory/MemoryManager.java`)
- Simulates memory allocation and deallocation
- Implements **First Fit algorithm** for allocation
- Tracks memory fragmentation
- Features:
  - Dynamic block allocation
  - Automatic block merging
  - Fragmentation analysis

---

## 🧪 Testing

### Test Suite (`test/SchedulerTest.java`)
Comprehensive JUnit 5 tests covering:

✅ All scheduling algorithms  
✅ Single and multiple processes  
✅ Arrival time handling  
✅ Priority and burst time ordering  
✅ Queue operations (FIFO, Priority)  
✅ Process state transitions  
✅ Metrics calculation  
✅ Edge cases  

**Run tests:**
```bash
mvn test
```

---

## 🚀 Quick Start

### 1. Build the Project
```bash
mvn clean compile
```

### 2. Run the Demo
```bash
mvn exec:java -Dexec.mainClass="com.unicore.scheduler.SchedulerDemo"
```

### 3. Run Tests
```bash
mvn test
```

### 4. Package as JAR
```bash
mvn package
```

---

## 📝 Usage Example

```java
import com.unicore.scheduler.algorithms.*;
import com.unicore.scheduler.core.*;
import java.util.*;

public class Example {
    public static void main(String[] args) {
        // Create processes
        List<Process> processes = new ArrayList<>();
        processes.add(new Process(1, "P1", 0, 8, 3));
        processes.add(new Process(2, "P2", 1, 4, 1));
        processes.add(new Process(3, "P3", 2, 2, 3));
        
        // Create scheduler
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        
        // Run simulation
        SchedulingResult result = scheduler.schedule(processes);
        
        // Display results
        System.out.println(result);
        System.out.println("Avg Waiting Time: " + result.getAverageWaitingTime());
        System.out.println("Avg Turnaround Time: " + result.getAverageTurnaroundTime());
    }
}
```

---

## 📈 Process Metrics

For each process, the simulator calculates:

- **Arrival Time (AT):** When process enters ready queue
- **Burst Time (BT):** Total CPU time needed
- **Completion Time (CT):** When process finishes
- **Turnaround Time (TAT):** CT - AT
- **Waiting Time (WT):** TAT - BT
- **Priority:** For priority-based scheduling

---

## 🔧 Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Language and runtime |
| JUnit 5 | 5.10.0 | Testing framework |
| SLF4J | 2.0.7 | Logging |
| JFreeChart | 1.5.3 | Visualization |
| AssertJ | 3.24.1 | Enhanced assertions |

---

## 📚 Extending the Library

### Add a New Scheduling Algorithm

1. Implement `SchedulingAlgorithm` interface
2. Implement `schedule()` method
3. Add test cases

Example:
```java
public class MLFQScheduler implements SchedulingAlgorithm {
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        // Your implementation
    }
    
    @Override
    public String getName() {
        return "Multi-Level Feedback Queue";
    }
    
    @Override
    public String getDescription() {
        return "Uses multiple queues with different priorities...";
    }
}
```

---

## 📊 Output Example

```
Algorithm: First Come First Served (FCFS)
Gantt Chart:
[P1: 0-8] [P2: 8-12] [P3: 12-14] [P4: 14-15] [P5: 15-18]

Process Metrics:
  P1(AT:0, BT:8, Pri:3, WT:0, TAT:8)
  P2(AT:1, BT:4, Pri:1, WT:7, TAT:11)
  P3(AT:2, BT:2, Pri:3, WT:10, TAT:12)
  P4(AT:3, BT:1, Pri:4, WT:11, TAT:12)
  P5(AT:4, BT:3, Pri:2, WT:11, TAT:14)

Overall Statistics:
  Average Waiting Time: 7.80
  Average Turnaround Time: 11.40
  Total Completion Time: 18
```

---

## 🎓 Learning Resources

- **CPU Scheduling:** Understand different scheduling strategies
- **OS Concepts:** Learn process management and context switching
- **Performance Analysis:** Compare algorithm efficiency
- **System Design:** Design scheduler for specific requirements

---

## 📄 License

Open source - Free to use and modify

---

## 👨‍💻 Contributing

To add features or improvements:
1. Create a feature branch
2. Write tests first (TDD approach)
3. Implement the feature
4. Ensure all tests pass
5. Submit pull request

---

## 🤝 Support

For issues or questions, check:
- Test cases for usage examples
- Algorithm descriptions above
- Javadoc in source files

---

**Happy Simulating!** 🚀
