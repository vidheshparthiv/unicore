package com.unicore.scheduler.core;

import java.util.*;

/**
 * Contains the results of CPU scheduling simulation.
 * Includes execution timeline, process metrics, and overall statistics.
 */
public class SchedulingResult {
    private final String algorithmName;
    private final List<Process> processes;
    private final List<GanttChartEntry> ganttChart;
    private double averageWaitingTime;
    private double averageTurnaroundTime;
    private int totalCompletionTime;

    public SchedulingResult(String algorithmName, List<Process> processes) {
        this.algorithmName = algorithmName;
        this.processes = new ArrayList<>(processes);
        this.ganttChart = new ArrayList<>();
    }

    /**
     * Add an entry to the Gantt chart.
     *
     * @param processId Process that was executed
     * @param startTime When execution started
     * @param endTime When execution ended
     */
    public void addGanttChartEntry(int processId, int startTime, int endTime) {
        ganttChart.add(new GanttChartEntry(processId, startTime, endTime));
    }

    /**
     * Calculate scheduling metrics. Must be called after all algorithms have finished scheduling.
     */
    public void finalizeMetrics() {
        calculateMetrics();
    }

    /**
     * Calculate scheduling metrics.
     */
    private void calculateMetrics() {
        if (processes.isEmpty()) {
            return;
        }

        double sumWaitingTime = 0;
        double sumTurnaroundTime = 0;
        int maxCompletionTime = 0;

        for (Process p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            
            sumWaitingTime += p.getWaitingTime();
            sumTurnaroundTime += p.getTurnaroundTime();
            maxCompletionTime = Math.max(maxCompletionTime, p.getCompletionTime());
        }

        this.averageWaitingTime = sumWaitingTime / processes.size();
        this.averageTurnaroundTime = sumTurnaroundTime / processes.size();
        this.totalCompletionTime = maxCompletionTime;
    }

    // Getters
    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    public List<GanttChartEntry> getGanttChart() {
        return new ArrayList<>(ganttChart);
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public int getTotalCompletionTime() {
        return totalCompletionTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===============================================\n");
        sb.append(String.format("Algorithm: %s\n", algorithmName));
        sb.append("-----------------------------------------------\n");
        sb.append("Gantt Chart:\n");
        
        for (GanttChartEntry entry : ganttChart) {
            sb.append(String.format("[P%d: %d-%d] ", entry.processId, entry.startTime, entry.endTime));
        }
        
        sb.append("\n\nProcess Metrics:\n");
        for (Process p : processes) {
            sb.append(String.format("  %s\n", p));
        }
        
        sb.append("\nOverall Statistics:\n");
        sb.append(String.format("  Average Waiting Time: %.2f\n", averageWaitingTime));
        sb.append(String.format("  Average Turnaround Time: %.2f\n", averageTurnaroundTime));
        sb.append(String.format("  Total Completion Time: %d\n", totalCompletionTime));
        sb.append("===============================================\n");
        
        return sb.toString();
    }

    /**
     * Entry in the Gantt chart showing process execution.
     */
    public static class GanttChartEntry {
        public final int processId;
        public final int startTime;
        public final int endTime;

        public GanttChartEntry(int processId, int startTime, int endTime) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
