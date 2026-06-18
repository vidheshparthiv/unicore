package com.unicore.scheduler;

import com.unicore.scheduler.algorithms.*;
import com.unicore.scheduler.core.*;
import com.unicore.scheduler.utils.SimulationLogger;
import java.util.*;

/**
 * Demo application showing how to use the CPU Scheduling Simulator Library.
 * Runs multiple scheduling algorithms on the same set of processes
 * and compares their performance.
 */
public class SchedulerDemo {

    public static void main(String[] args) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║      CPU SCHEDULING & OS SIMULATOR LIBRARY - DEMO          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        // Create test processes
        List<com.unicore.scheduler.core.Process> processes = createTestProcesses();

        // Display process information
        displayProcesses(processes);

        // Run all scheduling algorithms
        runAllAlgorithms(processes);
    }

    /**
     * Create a set of test processes for simulation.
     */
    private static List<com.unicore.scheduler.core.Process> createTestProcesses() {
        List<com.unicore.scheduler.core.Process> processes = new ArrayList<>();
        
        // Format: pid, name, arrivalTime, burstTime, priority
        processes.add(new com.unicore.scheduler.core.Process(1, "P1", 0, 8, 3));
        processes.add(new com.unicore.scheduler.core.Process(2, "P2", 1, 4, 1));
        processes.add(new com.unicore.scheduler.core.Process(3, "P3", 2, 2, 3));
        processes.add(new com.unicore.scheduler.core.Process(4, "P4", 3, 1, 4));
        processes.add(new com.unicore.scheduler.core.Process(5, "P5", 4, 3, 2));
        
        return processes;
    }

    /**
     * Display process information in a table format.
     */
    private static void displayProcesses(List<com.unicore.scheduler.core.Process> processes) {
        System.out.println("INPUT PROCESSES:");
        System.out.println("┌────┬──────────┬──────────┬──────────┬──────────┐");
        System.out.println("│ PID│   Name   │ Arrival  │  Burst   │ Priority │");
        System.out.println("├────┼──────────┼──────────┼──────────┼──────────┤");
        
        for (com.unicore.scheduler.core.Process p : processes) {
            System.out.printf("│ %d  │    %s    │    %d     │    %d     │    %d     │\n",
                    p.getPid(), p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority());
        }
        
        System.out.println("└────┴──────────┴──────────┴──────────┴──────────┘\n");
    }

    /**
     * Run all scheduling algorithms and display results.
     */
    private static void runAllAlgorithms(List<com.unicore.scheduler.core.Process> processes) {
        SchedulingAlgorithm[] algorithms = {
                new FCFSScheduler(),
                new SJFScheduler(),
                new RoundRobinScheduler(4),
                new PriorityScheduler(),
                new PreemptivePriorityScheduler()
        };

        List<SchedulingResult> results = new ArrayList<>();

        for (SchedulingAlgorithm algorithm : algorithms) {
            System.out.println("\n─ Running: " + algorithm.getName() + " ─");
            
            // Create fresh copies of processes for each algorithm
            List<com.unicore.scheduler.core.Process> processCopies = copyProcesses(processes);
            
            SchedulingResult result = algorithm.schedule(processCopies);
            results.add(result);
            
            // Print results
            System.out.println(result);
        }

        // Compare all algorithms
        compareResults(results);
    }

    /**
     * Create copies of processes for each algorithm.
     */
    private static List<com.unicore.scheduler.core.Process> copyProcesses(List<com.unicore.scheduler.core.Process> original) {
        List<com.unicore.scheduler.core.Process> copies = new ArrayList<>();
        for (com.unicore.scheduler.core.Process p : original) {
            copies.add(new com.unicore.scheduler.core.Process(p.getPid(), p.getName(), p.getArrivalTime(), 
                                  p.getBurstTime(), p.getPriority()));
        }
        return copies;
    }

    /**
     * Compare results of all algorithms.
     */
    private static void compareResults(List<SchedulingResult> results) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║              ALGORITHM COMPARISON SUMMARY                ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("│ Algorithm                  │ Avg Wait │ Avg TAT │ Total │");
        System.out.println("├────────────────────────────┼──────────┼─────────┼───────┤");
        
        SchedulingResult bestWait = results.get(0);
        SchedulingResult bestTAT = results.get(0);
        
        for (SchedulingResult result : results) {
            String name = result.getAlgorithmName();
            if (name.length() > 27) {
                name = name.substring(0, 24) + "...";
            }
            
            System.out.printf("│ %-26s │ %7.2f │ %6.2f │ %5d │\n",
                    name,
                    result.getAverageWaitingTime(),
                    result.getAverageTurnaroundTime(),
                    result.getTotalCompletionTime());
            
            if (result.getAverageWaitingTime() < bestWait.getAverageWaitingTime()) {
                bestWait = result;
            }
            
            if (result.getAverageTurnaroundTime() < bestTAT.getAverageTurnaroundTime()) {
                bestTAT = result;
            }
        }
        
        System.out.println("╠════════════════════════════╩══════════╩═════════╩═══════╣");
        System.out.println("║ Best for Waiting Time: " + bestWait.getAlgorithmName());
        System.out.println("║ Best for Turnaround Time: " + bestTAT.getAlgorithmName());
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }
}
