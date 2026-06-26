# UniCore - CPU Scheduling & OS Simulator Library

A comprehensive Java library for simulating CPU scheduling algorithms and Operating System operations. Built with Java 21, this project provides realistic simulations of various CPU scheduling strategies, memory management, and process handling used in modern operating systems.

##  Project Overview

This library provides a complete simulation framework for:
- **CPU Scheduling Algorithms** (FCFS, SJF, Round Robin, Priority, Preemptive Priority)
- **Process Management** (Process states, lifecycle, metrics)
- **Memory Management** (Dynamic allocation, fragmentation analysis)
- **Performance Analysis** (Waiting time, turnaround time, completion time)

## 📦 What's Included

### Core Components
- ✅ **Process Model** - Full process state management
- ✅ **5 Scheduling Algorithms** - Preemptive & non-preemptive
- ✅ **Scheduler Controller** - Manages scheduling simulation
- ✅ **Memory Manager** - Memory allocation and deallocation
- ✅ **Performance Metrics** - Comprehensive analysis tools

### Testing
- ✅ **Full JUnit 5 Test Suite** - 10+ test cases per algorithm
- ✅ **Demo Application** - Shows all algorithms in action
- ✅ **Edge Case Coverage** - Single process, arrival times, priorities

## 🚀 Quick Start

### Build
```bash
mvn clean compile
```

### Run Demo (Compare all algorithms)
```bash
mvn exec:java -Dexec.mainClass="com.unicore.scheduler.SchedulerDemo"
```

### Run Tests
```bash
mvn test
```

## 📊 Scheduling Algorithms

| Algorithm | Type | Use Case | Pros | Cons |
|-----------|------|----------|------|------|
| **FCFS** | Non-preemptive | Simple systems | Fair, easy | High wait time |
| **SJF** | Non-preemptive | Batch jobs | Optimal wait time | Starvation risk |
| **Round Robin** | Preemptive | Time-sharing | Fair, interactive | More switches |
| **Priority** | Non-preemptive | Real-time | Handles urgency | Starvation |
| **Preemptive Priority** | Preemptive | Real-time critical | Best response | More overhead |

## 📁 Project Structure

See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for detailed documentation of:
- Directory layout
- Component descriptions
- Usage examples
- Extension guide

## 💻 Technology Stack

- **Java 21** - Latest features and performance
- **Maven** - Build automation
- **JUnit 5** - Testing framework
- **SLF4J** - Logging
- **JFreeChart** - Visualization (optional)

## 📝 Usage Example

```java
import com.unicore.scheduler.algorithms.*;
import com.unicore.scheduler.core.*;

// Create processes
List<Process> processes = new ArrayList<>();
processes.add(new Process(1, "P1", 0, 8, 3));
processes.add(new Process(2, "P2", 1, 4, 1));

// Schedule and run
SchedulingAlgorithm scheduler = new FCFSScheduler();
SchedulingResult result = scheduler.schedule(processes);

// Analyze results
System.out.println("Average Waiting Time: " + result.getAverageWaitingTime());
System.out.println("Average Turnaround Time: " + result.getAverageTurnaroundTime());
```

## 🧪 Testing

Comprehensive test suite covering:
- All 5 scheduling algorithms
- Process state transitions
- Memory allocation
- Edge cases (single process, zero arrival time, etc.)
- Queue operations
- Metrics calculation

Run: `mvn test`

## 📈 Example Output

```
Algorithm: First Come First Served (FCFS)
Gantt Chart: [P1: 0-8] [P2: 8-12] [P3: 12-14] [P4: 14-15] [P5: 15-18]

Process Metrics:
  P1(AT:0, BT:8, Pri:3, WT:0, TAT:8)
  P2(AT:1, BT:4, Pri:1, WT:7, TAT:11)

Average Waiting Time: 7.80
Average Turnaround Time: 11.40
Total Completion Time: 18
```

## 🎓 Learning Features

Perfect for:
- **Students** learning OS concepts
- **Developers** building schedulers
- **Researchers** comparing algorithms
- **Educators** teaching process scheduling

## 🔧 Extending the Library

### Add a New Algorithm
1. Create class implementing `SchedulingAlgorithm`
2. Implement `schedule()` method
3. Write test cases
4. See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for example

## 📚 Key Classes

| Class | Purpose |
|-------|---------|
| `Process` | Process entity with state |
| `Scheduler` | Main scheduling controller |
| `SchedulingAlgorithm` | Interface for algorithms |
| `SchedulingResult` | Results and metrics |
| `ProcessQueue` | Queue management |
| `MemoryManager` | Memory allocation |

## 📄 License

Open Source - Free to use and modify

## 🤝 Support

- Check test cases for usage examples
- See PROJECT_STRUCTURE.md for detailed docs
- Review javadoc in source files

---

**Ready to simulate CPU scheduling!** 🚀
