package model;

import java.time.LocalTime;

/**
 * LayoverPolicy defines the business rules for acceptable layover durations
 * between connecting routes in a Connection.
 * 
 * According to Iteration 3 requirements:
 * - During the day (6:00-22:00): layovers should be 1-2 hours (60-120 minutes)
 * - After hours (22:00-6:00): layovers should be 30 minutes or less
 * 
 * This class matches the domain model from Iteration 3.
 */
public class LayoverPolicy {
    /**
     * Maximum acceptable layover during daytime hours (6:00-22:00).
     * According to requirements: "During the day it can be OK to have a layover for 1-2 hours."
     */
    public static final int MAX_DAYTIME_LAYOVER_MINUTES = 120; // 2 hours
    
    /**
     * Minimum acceptable layover during daytime hours.
     * According to requirements: layovers should be 1-2 hours during the day.
     */
    public static final int MIN_DAYTIME_LAYOVER_MINUTES = 60; // 1 hour
    
    /**
     * Maximum acceptable layover after hours (22:00-6:00).
     * According to requirements: "we don't want a layover for more than 30 minutes when after hours."
     */
    public static final int MAX_AFTERHOURS_LAYOVER_MINUTES = 30; // 30 minutes
    
    /**
     * Start of daytime hours.
     */
    public static final LocalTime DAYTIME_START = LocalTime.of(6, 0);
    
    /**
     * End of daytime hours.
     */
    public static final LocalTime DAYTIME_END = LocalTime.of(22, 0);

    private LayoverPolicy() {}

    /**
     * Checks if a layover duration is acceptable according to the policy.
     * 
     * @param layoverMinutes the duration of the layover in minutes
     * @param layoverTime the time when the layover occurs (arrival time of first route)
     * @return true if the layover is acceptable, false otherwise
     */
    public static boolean isAcceptableLayover(int layoverMinutes, LocalTime layoverTime) {
        if (layoverMinutes < 0) {
            return false;
        }

        if (isAfterHours(layoverTime)) {
            // After hours: maximum 30 minutes
            return layoverMinutes <= MAX_AFTERHOURS_LAYOVER_MINUTES;
        } else {
            // Daytime: 1-2 hours (60-120 minutes)
            return layoverMinutes >= MIN_DAYTIME_LAYOVER_MINUTES && 
                   layoverMinutes <= MAX_DAYTIME_LAYOVER_MINUTES;
        }
    }

    /**
     * Checks if a given time is considered "after hours" (outside 6:00-22:00).
     * 
     * @param time the time to check
     * @return true if the time is before 6:00 or after 22:00, false otherwise
     */
    public static boolean isAfterHours(LocalTime time) {
        return time.isBefore(DAYTIME_START) || time.isAfter(DAYTIME_END);
    }
    
    /**
     * Gets a human-readable description of the layover policy.
     * 
     * @return a string describing the policy rules
     */
    public static String getPolicyDescription() {
        return "Layover Policy: " +
               "During the day (6:00-22:00), layovers must be 1-2 hours. " +
               "After hours (22:00-6:00), layovers must be 30 minutes or less.";
    }
}
