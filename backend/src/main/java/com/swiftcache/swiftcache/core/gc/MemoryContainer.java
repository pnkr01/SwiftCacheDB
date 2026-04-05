package com.swiftcache.swiftcache.core.gc;

public class MemoryContainer {
    private final Object data;
    private long lastAccessedTime;
    private long lastExecutionLatencyNanos;
    private int sizeInBytes;

    public MemoryContainer(Object data) {
        this.data = data;
        this.lastAccessedTime = System.currentTimeMillis();
        calculateSize();
    }


    private void calculateSize() {
        // Simple estimation: 2 bytes per char for strings
        if (data instanceof String) {
            this.sizeInBytes = ((String) data).length() * 2;
        } else {
            // For complex objects, we'll use a rough estimate based on items
            this.sizeInBytes = 1024; // Default 1KB for new structures
        }
    }

    public Object getData(long latencyNanos) {
        this.lastAccessedTime = System.currentTimeMillis();
        this.lastExecutionLatencyNanos = latencyNanos;
        return data;
    }

    public long getLastLatency() { return lastExecutionLatencyNanos; }
    public int getSizeInBytes() { return sizeInBytes; }

    // Retrieves the data and silently updates the stopwatch.
    public Object getData() {
        this.lastAccessedTime = System.currentTimeMillis();
        return data;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    // Returns data WITHOUT updating the stopwatch (used by the dashboard)
    public Object peekData() {
        return data;
    }
}