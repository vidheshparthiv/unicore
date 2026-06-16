package com.unicore.scheduler.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.unicore.scheduler.core.SchedulingResult;

/**
 * Utility class for logging and reporting simulation results.
 */
public class SimulationLogger {
    private static final Logger logger = LoggerFactory.getLogger(SimulationLogger.class);

    /**
     * Log simulation start.
     */
    public static void logSimulationStart(String algorithmName) {
        logger.info("========================================");
        logger.info("Starting CPU Scheduling Simulation");
        logger.info("Algorithm: {}", algorithmName);
        logger.info("========================================");
    }

    /**
     * Log simulation result.
     */
    public static void logResult(SchedulingResult result) {
        logger.info("Average Waiting Time: {}", String.format("%.2f", result.getAverageWaitingTime()));
        logger.info("Average Turnaround Time: {}", String.format("%.2f", result.getAverageTurnaroundTime()));
        logger.info("Total Completion Time: {}", result.getTotalCompletionTime());
    }

    /**
     * Get formatted report.
     */
    public static String getFormattedReport(SchedulingResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.toString());
        return sb.toString();
    }
}
