package com.unicore.scheduler.algorithms;

import com.unicore.scheduler.core.Process;
import com.unicore.scheduler.core.SchedulingAlgorithm;
import com.unicore.scheduler.core.SchedulingResult;
import java.util.*;

/**
 * Priority Scheduling Algorithm (Non-Preemptive).
 * Processes with higher priority (lower priority number) are executed first.
 * Once a process starts execution, it runs until completion.
 */
public class PriorityScheduler implements SchedulingAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> processList = new ArrayList<>(processes);
        SchedulingResult result = new SchedulingResult(getName(), processList);
        
        int currentTime = 0;
        List<Process> remaining = new ArrayList<>(processList);
        remaining.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        while (!remaining.isEmpty()) {
            // Get processes that have arrived
            List<Process> ready = new ArrayList<>();
            for (Process p : remaining) {
                if (p.getArrivalTime() <= currentTime) {
                    ready.add(p);
                }
            }
            
            if (ready.isEmpty()) {
                // No process has arrived yet, advance time
                int nextArrival = remaining.get(0).getArrivalTime();
                currentTime = nextArrival;
                continue;
            }
            
            // Select process with highest priority (lowest priority number)
            Process selected = ready.stream()
                    .min(Comparator.comparingInt(Process::getPriority))
                    .orElseThrow();
            
            // Execute the process
            int startTime = currentTime;
            currentTime += selected.getBurstTime();
            selected.setCompletionTime(currentTime);
            selected.setState(Process.ProcessState.COMPLETED);
            
            result.addGanttChartEntry(selected.getPid(), startTime, currentTime);
            remaining.remove(selected);
        }
        
        result.finalizeMetrics();
        return result;
    }

    @Override
    public String getName() {
        return "Priority Scheduling (Non-Preemptive)";
    }

    @Override
    public String getDescription() {
        return "Non-preemptive algorithm that selects the process with the highest priority " +
               "(lowest priority number) from the ready queue. Each process runs to completion.";
    }
}
