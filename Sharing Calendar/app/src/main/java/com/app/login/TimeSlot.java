package com.app.login;

public class TimeSlot {
    private long startTime;  // Start time in milliseconds
    private long endTime;    // End time in milliseconds

    // Constructor
    public TimeSlot(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter methods
    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    // Optional: Method to check if two time slots overlap
    public boolean overlapsWith(TimeSlot other) {
        return (this.startTime < other.endTime) && (this.endTime > other.startTime);
    }

    // Method to merge overlapping time slots
    public TimeSlot mergeWith(TimeSlot other) {
        // Merge two overlapping time slots into one
        long newStartTime = Math.min(this.startTime, other.startTime);
        long newEndTime = Math.max(this.endTime, other.endTime);
        return new TimeSlot(newStartTime, newEndTime);
    }
}
