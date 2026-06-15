package com.unicore.scheduler;

import com.unicore.scheduler.algorithms.*;
import com.unicore.scheduler.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CPU scheduling algorithms.
 * Verifies correctness of scheduling results for various scenarios.
 */
public class SchedulerTest {
    
    private List<com.unicore.scheduler.core.Process> testProcesses;
    
    @BeforeEach
    public void setUp() {
        testProcesses = new ArrayList<>();
        // Create test processes
        // Format: pid, name, arrivalTime, burstTime, priority
        testProcesses.add(new com.unicore.scheduler.core.Process(1, "P1", 0, 8, 3));
        testProcesses.add(new com.unicore.scheduler.core.Process(2, "P2", 1, 4, 1));
        testProcesses.add(new com.unicore.scheduler.core.Process(3, "P3", 2, 2, 3));
        testProcesses.add(new com.unicore.scheduler.core.Process(4, "P4", 3, 1, 4));
        testProcesses.add(new com.unicore.scheduler.core.Process(5, "P5", 4, 3, 2));
    }

    @Test
    public void testFCFSScheduling() {
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        assertNotNull(result);
        assertEquals(5, result.getProcesses().size());
        assertEquals("First Come First Served (FCFS)", result.getAlgorithmName());
        
        // Verify total completion time
        assertEquals(18, result.getTotalCompletionTime());
        
        // Verify Gantt chart entries
        assertTrue(result.getGanttChart().size() > 0);
        
        System.out.println(result);
    }

    @Test
    public void testSJFScheduling() {
        SchedulingAlgorithm scheduler = new SJFScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        assertNotNull(result);
        assertEquals(5, result.getProcesses().size());
        assertEquals("Shortest Job First (SJF)", result.getAlgorithmName());
        
        System.out.println(result);
    }

    @Test
    public void testRoundRobinScheduling() {
        SchedulingAlgorithm scheduler = new RoundRobinScheduler(4);
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        assertNotNull(result);
        assertEquals(5, result.getProcesses().size());
        assertTrue(result.getAlgorithmName().contains("Round Robin"));
        
        System.out.println(result);
    }

    @Test
    public void testPriorityScheduling() {
        SchedulingAlgorithm scheduler = new PriorityScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        assertNotNull(result);
        assertEquals(5, result.getProcesses().size());
        assertEquals("Priority Scheduling (Non-Preemptive)", result.getAlgorithmName());
        
        System.out.println(result);
    }

    @Test
    public void testPreemptivePriorityScheduling() {
        SchedulingAlgorithm scheduler = new PreemptivePriorityScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        assertNotNull(result);
        assertEquals(5, result.getProcesses().size());
        assertEquals("Preemptive Priority Scheduling", result.getAlgorithmName());
        
        System.out.println(result);
    }

    @Test
    public void testAllProcessesCompleted() {
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        // Verify all processes have completion times
        for (com.unicore.scheduler.core.Process p : result.getProcesses()) {
            assertTrue(p.getCompletionTime() > 0, "Process " + p.getName() + " should be completed");
            assertTrue(p.getTurnaroundTime() > 0, "Process " + p.getName() + " should have turnaround time");
        }
    }

    @Test
    public void testSingleProcess() {
        List<com.unicore.scheduler.core.Process> singleProcess = new ArrayList<>();
        singleProcess.add(new com.unicore.scheduler.core.Process(1, "P1", 0, 10, 1));
        
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        SchedulingResult result = scheduler.schedule(singleProcess);
        
        assertEquals(1, result.getProcesses().size());
        assertEquals(10, result.getTotalCompletionTime());
    }

    @Test
    public void testProcessArrivalTimes() {
        List<com.unicore.scheduler.core.Process> processes = new ArrayList<>();
        processes.add(new com.unicore.scheduler.core.Process(1, "P1", 0, 5, 1));
        processes.add(new com.unicore.scheduler.core.Process(2, "P2", 5, 5, 1));
        processes.add(new com.unicore.scheduler.core.Process(3, "P3", 10, 5, 1));
        
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        SchedulingResult result = scheduler.schedule(processes);
        
        // P1: 0-5, P2: 5-10, P3: 10-15
        assertEquals(15, result.getTotalCompletionTime());
    }

    @Test
    public void testAverageMetrics() {
        SchedulingAlgorithm scheduler = new FCFSScheduler();
        SchedulingResult result = scheduler.schedule(testProcesses);
        
        double avgWait = result.getAverageWaitingTime();
        double avgTurnaround = result.getAverageTurnaroundTime();
        
        assertTrue(avgWait >= 0, "Average waiting time should be non-negative");
        assertTrue(avgTurnaround > 0, "Average turnaround time should be positive");
        assertTrue(avgTurnaround >= avgWait, "Turnaround time should be >= waiting time");
    }

    @Test
    public void testProcessQueue() {
        ProcessQueue fifoQueue = new ProcessQueue(ProcessQueue.QueueType.FIFO);
        
        com.unicore.scheduler.core.Process p1 = new com.unicore.scheduler.core.Process(1, "P1", 0, 5, 1);
        com.unicore.scheduler.core.Process p2 = new com.unicore.scheduler.core.Process(2, "P2", 1, 4, 2);
        com.unicore.scheduler.core.Process p3 = new com.unicore.scheduler.core.Process(3, "P3", 2, 2, 3);
        
        fifoQueue.enqueue(p1);
        fifoQueue.enqueue(p2);
        fifoQueue.enqueue(p3);
        
        assertEquals(3, fifoQueue.size());
        assertEquals(p1, fifoQueue.dequeue());
        assertEquals(p2, fifoQueue.dequeue());
        assertEquals(1, fifoQueue.size());
    }

    @Test
    public void testPriorityQueue() {
        ProcessQueue priorityQueue = new ProcessQueue(ProcessQueue.QueueType.PRIORITY);
        
        com.unicore.scheduler.core.Process p1 = new com.unicore.scheduler.core.Process(1, "P1", 0, 5, 3);
        com.unicore.scheduler.core.Process p2 = new com.unicore.scheduler.core.Process(2, "P2", 1, 4, 1);
        com.unicore.scheduler.core.Process p3 = new com.unicore.scheduler.core.Process(3, "P3", 2, 2, 2);
        
        priorityQueue.enqueue(p1);
        priorityQueue.enqueue(p2);
        priorityQueue.enqueue(p3);
        
        // Should dequeue in priority order: p2(1), p3(2), p1(3)
        assertEquals(p2, priorityQueue.dequeue());
        assertEquals(p3, priorityQueue.dequeue());
        assertEquals(p1, priorityQueue.dequeue());
    }

    @Test
    public void testProcessExecution() {
        com.unicore.scheduler.core.Process p = new com.unicore.scheduler.core.Process(1, "P1", 0, 10, 1);
        
        assertEquals(10, p.getRemainingBurstTime());
        
        int executed = p.execute(3);
        assertEquals(3, executed);
        assertEquals(7, p.getRemainingBurstTime());
        assertFalse(p.isCompleted());
        
        executed = p.execute(7);
        assertEquals(7, executed);
        assertEquals(0, p.getRemainingBurstTime());
        assertTrue(p.isCompleted());
    }
}
