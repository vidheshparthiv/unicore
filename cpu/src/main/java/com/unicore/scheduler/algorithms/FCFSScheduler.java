package com.unicore.scheduler.algorithms;

import com.unicore.scheduler.core.Process;
import com.unicore.scheduler.core.SchedulingAlgorithm;
import com.unicore.scheduler.core.SchedulingResult;
import java.util.*;

/**
 * First Come First Served (FCFS) Scheduling Algorithm.
 * Processes are executed in the order they arrive in the ready queue.
 * Non-preemptive: once a process starts, it runs until completion.
 */
public class FCFSScheduler implements SchedulingAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> processList = new ArrayList<>(processes);
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        SchedulingResult result = new SchedulingResult(getName(), processList);
        int currentTime = 0;
        
        for (Process process : processList) {
            // Wait for process to arrive
            if (currentTime < process.getArrivalTime()) {
                currentTime = process.getArrivalTime();
            }
            
            // Execute the process
            int startTime = currentTime;
            int executeTime = process.getBurstTime();
            currentTime += executeTime;
            
            // Update process metrics
            process.setCompletionTime(currentTime);
            process.setState(Process.ProcessState.COMPLETED);
            
            // Add to Gantt chart
            result.addGanttChartEntry(process.getPid(), startTime, currentTime);
        }
        
        result.finalizeMetrics();
        return result;
    }

    @Override
    public String getName() {
        return "First Come First Served (FCFS)";
    }

    @Override
    public String getDescription() {
        return "Non-preemptive algorithm where processes are executed in arrival order. " +
               "Once a process starts execution, it runs to completion without interruption.";
    }
}
