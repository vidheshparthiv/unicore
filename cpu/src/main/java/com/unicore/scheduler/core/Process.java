package com.unicore.scheduler.core;

import java.util.Objects;

/**
 * Represents a process in the CPU scheduling simulator.
 * Contains all necessary information for process scheduling.
 */
public class Process {
    private final int pid;  // Process ID
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private ProcessState state;
    private int remainingBurstTime;

    /**
     * Create a process with basic scheduling information.
     *
     * @param pid Process ID
     * @param name Process name
     * @param arrivalTime When the process arrives in the ready queue
     * @param burstTime Total CPU time required
     * @param priority Priority level (lower number = higher priority)
     */
    public Process(int pid, String name, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.state = ProcessState.NEW;
        this.remainingBurstTime = burstTime;
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
    }

    // Getters
    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public ProcessState getState() {
        return state;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    // Setters
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void setRemainingBurstTime(int remainingBurstTime) {
        this.remainingBurstTime = Math.max(0, remainingBurstTime);
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * Execute the process for a given time slice.
     *
     * @param timeSlice Amount of CPU time to execute
     * @return Actual time executed (may be less if process completes)
     */
    public int execute(int timeSlice) {
        int executed = Math.min(timeSlice, remainingBurstTime);
        remainingBurstTime -= executed;
        if (remainingBurstTime == 0) {
            state = ProcessState.COMPLETED;
        }
        return executed;
    }

    /**
     * Check if the process has completed.
     */
    public boolean isCompleted() {
        return remainingBurstTime <= 0;
    }

    @Override
    public String toString() {
        return String.format("P%d(AT:%d, BT:%d, Pri:%d, WT:%d, TAT:%d)",
                pid, arrivalTime, burstTime, priority, waitingTime, turnaroundTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Process process = (Process) o;
        return pid == process.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    /**
     * Process states in the scheduler.
     */
    public enum ProcessState {
        NEW, READY, RUNNING, WAITING, COMPLETED
    }
}
