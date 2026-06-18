package com.unicore.scheduler.algorithms;

import com.unicore.scheduler.core.Process;
import com.unicore.scheduler.core.SchedulingAlgorithm;
import com.unicore.scheduler.core.SchedulingResult;
import java.util.*;

/**
 * Preemptive Priority Scheduling Algorithm.
 * Processes are selected based on priority (lower number = higher priority).
 * If a higher priority process arrives, the current process is preempted.
 */
public class PreemptivePriorityScheduler implements SchedulingAlgorithm {
    
    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> processList = new ArrayList<>(processes);
        SchedulingResult result = new SchedulingResult(getName(), processList);
        
        int currentTime = 0;
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt(Process::getPriority));
        
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        int processIndex = 0;
        Process current = null;
        
        while (processIndex < processList.size() || !readyQueue.isEmpty() || current != null) {
            // Add newly arrived processes to ready queue
            while (processIndex < processList.size() && 
                   processList.get(processIndex).getArrivalTime() <= currentTime) {
                readyQueue.offer(processList.get(processIndex));
                processIndex++;
            }
            
            // Check if current process should be preempted
            if (current != null && !readyQueue.isEmpty()) {
                Process next = readyQueue.peek();
                if (next.getPriority() < current.getPriority()) {
                    // Higher priority process arrived, preempt current
                    readyQueue.offer(current);
                    current = null;
                }
            }
            
            // Select next process if none is running
            if (current == null) {
                if (readyQueue.isEmpty()) {
                    if (processIndex < processList.size()) {
                        currentTime = processList.get(processIndex).getArrivalTime();
                    } else {
                        break;
                    }
                } else {
                    current = readyQueue.poll();
                }
            }
            
            // Execute current process for 1 time unit
            if (current != null) {
                int startTime = currentTime;
                current.execute(1);
                currentTime++;
                
                if (current.isCompleted()) {
                    current.setCompletionTime(currentTime);
                    current.setState(Process.ProcessState.COMPLETED);
                    result.addGanttChartEntry(current.getPid(), startTime, currentTime);
                    current = null;
                } else {
                    result.addGanttChartEntry(current.getPid(), startTime, currentTime);
                }
            }
        }
        
        result.finalizeMetrics();
        return result;
    }

    @Override
    public String getName() {
        return "Preemptive Priority Scheduling";
    }

    @Override
    public String getDescription() {
        return "Preemptive algorithm where the highest priority (lowest number) process " +
               "is selected. If a higher priority process arrives, the current process is preempted.";
    }
}
