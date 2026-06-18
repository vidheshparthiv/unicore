package com.unicore.scheduler.core;

import java.util.*;

/**
 * Manages the queue of processes ready for execution.
 * Supports different ordering strategies (FIFO, Priority, etc.)
 */
public class ProcessQueue {
    private final Queue<Process> queue;
    private final QueueType type;

    public enum QueueType {
        FIFO,           // First In First Out
        PRIORITY,       // By priority (lower number = higher priority)
        SHORTEST_JOB    // By remaining burst time
    }

    public ProcessQueue(QueueType type) {
        this.type = type;
        this.queue = initializeQueue(type);
    }

    private Queue<Process> initializeQueue(QueueType type) {
        return switch (type) {
            case FIFO -> new LinkedList<>();
            case PRIORITY -> new PriorityQueue<>(Comparator.comparingInt(Process::getPriority));
            case SHORTEST_JOB -> new PriorityQueue<>(Comparator.comparingInt(Process::getRemainingBurstTime));
        };
    }

    /**
     * Add a process to the queue.
     */
    public void enqueue(Process process) {
        queue.offer(process);
    }

    /**
     * Remove and return the next process from the queue.
     */
    public Process dequeue() {
        return queue.poll();
    }

    /**
     * Get the next process without removing it.
     */
    public Process peek() {
        return queue.peek();
    }

    /**
     * Check if the queue is empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Get the current size of the queue.
     */
    public int size() {
        return queue.size();
    }

    /**
     * Get the queue type.
     */
    public QueueType getType() {
        return type;
    }

    /**
     * Clear all processes from the queue.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * Get all processes currently in the queue.
     */
    public List<Process> getAll() {
        return new ArrayList<>(queue);
    }
}
