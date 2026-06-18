package com.unicore.scheduler.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Main CPU scheduler that manages process execution simulation.
 * Handles the scheduling timeline and executes processes according to an algorithm.
 */
public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    
    private final SchedulingAlgorithm algorithm;
    private final ProcessQueue readyQueue;
    private Process currentProcess;
    private int currentTime;

    public Scheduler(SchedulingAlgorithm algorithm, ProcessQueue.QueueType queueType) {
        this.algorithm = algorithm;
        this.readyQueue = new ProcessQueue(queueType);
        this.currentTime = 0;
        this.currentProcess = null;
    }

    /**
     * Add a process to the scheduler.
     */
    public void addProcess(Process process) {
        logger.debug("Added process: {}", process);
        readyQueue.enqueue(process);
    }

    /**
     * Add multiple processes to the scheduler.
     */
    public void addProcesses(List<Process> processes) {
        for (Process p : processes) {
            addProcess(p);
        }
    }

    /**
     * Execute scheduling using the configured algorithm.
     */
    public SchedulingResult execute(List<Process> processes) {
        logger.info("Starting scheduling with algorithm: {}", algorithm.getName());
        return algorithm.schedule(processes);
    }

    /**
     * Get the current system time.
     */
    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * Set the current system time.
     */
    public void setCurrentTime(int time) {
        this.currentTime = time;
    }

    /**
     * Advance the system time by a given amount.
     */
    public void advanceTime(int amount) {
        this.currentTime += amount;
    }

    /**
     * Get the current running process.
     */
    public Process getCurrentProcess() {
        return currentProcess;
    }

    /**
     * Set the current running process.
     */
    public void setCurrentProcess(Process process) {
        this.currentProcess = process;
        if (process != null) {
            process.setState(Process.ProcessState.RUNNING);
        }
    }

    /**
     * Check if the ready queue has processes waiting.
     */
    public boolean hasReadyProcesses() {
        return !readyQueue.isEmpty();
    }

    /**
     * Get the next process from the ready queue.
     */
    public Process getNextProcess() {
        return readyQueue.dequeue();
    }

    /**
     * Get the name of the current algorithm.
     */
    public String getAlgorithmName() {
        return algorithm.getName();
    }
}
