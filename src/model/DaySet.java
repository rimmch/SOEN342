package model;

import java.time.DayOfWeek;

public class DaySet {
    private int dayPattern;

    public DaySet(int dayPattern) {
        this.dayPattern = dayPattern;
    }

    public boolean isOperatingOn(DayOfWeek dayOfWeek) {
        int dayIndex = dayOfWeek.getValue() - 1;
        return (dayPattern & (1 << dayIndex)) != 0;
    }

    public void setOperatingDay(DayOfWeek dayOfWeek, boolean operating) {
        int dayIndex = dayOfWeek.getValue() - 1;
        if (operating) {
            dayPattern |= (1 << dayIndex);
        } else {
            dayPattern &= ~(1 << dayIndex);
        }
    }

    public int getDayPattern() {
        return dayPattern;
    }

    public void setDayPattern(int dayPattern) {
        this.dayPattern = dayPattern;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (isOperatingOn(day)) {
                sb.append(day.name().substring(0, 3)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}