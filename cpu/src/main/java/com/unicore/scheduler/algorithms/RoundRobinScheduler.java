package com.unicore.scheduler.algorithms;

import com.unicore.scheduler.core.Process;
import com.unicore.scheduler.core.SchedulingAlgorithm;
import com.unicore.scheduler.core.SchedulingResult;
import java.util.*;

/**
 * Round Robin (RR) Scheduling Algorithm.
 * Each process gets a fixed time slice (time quantum) to execute.
 * After the time slice expires, the process goes to the back of the ready queue.
 * Preemptive algorithm: provides fair CPU time distribution.
 */
public class RoundRobinScheduler implements SchedulingAlgorithm {
    
    private static final int DEFAULT_TIME_QUANTUM = 4;
    private final int timeQuantum;

    public RoundRobinScheduler() {
        this.timeQuantum = DEFAULT_TIME_QUANTUM;
    }

    public RoundRobinScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> processList = new ArrayList<>(processes);
        SchedulingResult result = new SchedulingResult(
                getName() + " (Quantum: " + timeQuantum + ")", processList);
        
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        
        // Sort by arrival time
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        // Add initial processes
        for (Process p : processList) {
            if (p.getArrivalTime() == 0) {
                readyQueue.offer(p);
            }
        }
        
        int processIndex = 0;
        
        while (!readyQueue.isEmpty() || processIndex < processList.size()) {
            // Add newly arrived processes
            while (processIndex < processList.size() && 
                   processList.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = processList.get(processIndex);
                if (!readyQueue.contains(p)) {
                    readyQueue.offer(p);
                }
                processIndex++;
            }
            
            if (readyQueue.isEmpty()) {
                // No ready process, advance time
                if (processIndex < processList.size()) {
                    currentTime = processList.get(processIndex).getArrivalTime();
                }
                continue;
            }
            
            // Execute current process
            Process current = readyQueue.poll();
            int startTime = currentTime;
            int executed = current.execute(timeQuantum);
            currentTime += executed;
            
            result.addGanttChartEntry(current.getPid(), startTime, currentTime);
            
            // Add newly arrived processes
            while (processIndex < processList.size() && 
                   processList.get(processIndex).getArrivalTime() <= currentTime) {
                Process p = processList.get(processIndex);
                if (!readyQueue.contains(p) && !p.isCompleted()) {
                    readyQueue.offer(p);
                }
                processIndex++;
            }
            
            // If process not completed, add back to queue
            if (!current.isCompleted()) {
                readyQueue.offer(current);
            } else {
                current.setCompletionTime(currentTime);
                current.setState(Process.ProcessState.COMPLETED);
            }
        }
        
        result.finalizeMetrics();
        return result;
    }

    @Override
    public String getName() {
        return "Round Robin (RR)";
    }

    @Override
    public String getDescription() {
        return "Preemptive algorithm where each process gets a fixed time quantum to execute. " +
               "If the process doesn't complete within the quantum, it goes to the back of the queue.";
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }
}
