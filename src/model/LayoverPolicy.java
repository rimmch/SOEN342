package model;

import java.time.LocalTime;

public class LayoverPolicy {
    private static final int MAX_DAYTIME_LAYOVER_MINUTES = 120; // 2 hours
    private static final int MAX_AFTERHOURS_LAYOVER_MINUTES = 30; // 30 minutes
    private static final LocalTime DAYTIME_START = LocalTime.of(6, 0);
    private static final LocalTime DAYTIME_END = LocalTime.of(22, 0);

    public static boolean isAcceptableLayover(int layoverMinutes, LocalTime layoverTime) {
        if (isAfterHours(layoverTime)) {
            return layoverMinutes <= MAX_AFTERHOURS_LAYOVER_MINUTES;
        } else {
            return layoverMinutes <= MAX_DAYTIME_LAYOVER_MINUTES;
        }
    }

    private static boolean isAfterHours(LocalTime time) {
        return time.isBefore(DAYTIME_START) || time.isAfter(DAYTIME_END);
    }
}