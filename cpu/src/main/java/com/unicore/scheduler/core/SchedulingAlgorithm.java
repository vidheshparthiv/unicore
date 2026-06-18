package com.unicore.scheduler.core;

import java.util.List;

/**
 * Interface for different CPU scheduling algorithms.
 * Implementations provide different strategies for process scheduling.
 */
public interface SchedulingAlgorithm {
    
    /**
     * Schedule processes using this algorithm.
     *
     * @param processes List of processes to schedule
     * @return Scheduling result with execution timeline and metrics
     */
    SchedulingResult schedule(List<Process> processes);
    
    /**
     * Get the name of this scheduling algorithm.
     */
    String getName();
    
    /**
     * Get a description of how this algorithm works.
     */
    String getDescription();
}
