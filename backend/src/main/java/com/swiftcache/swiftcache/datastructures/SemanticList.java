package com.swiftcache.swiftcache.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SemanticList {

    private final LinkedList<String> contextWindow;
    private final int maxCapacity;
    private final boolean hasSystemPrompt;

    // ReadWriteLock allows multiple agents to READ the context simultaneously,
    // but ensures only one can WRITE at a time to prevent data corruption.
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SemanticList(int maxCapacity, boolean hasSystemPrompt) {
        this.contextWindow = new LinkedList<>();
        this.maxCapacity = maxCapacity;
        this.hasSystemPrompt = hasSystemPrompt;
    }

    //appends a new message into context
    public void push(String message) {
        lock.writeLock().lock();
        try {
            contextWindow.addLast(message);
            enforceCapacity();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void enforceCapacity() {
        while (contextWindow.size() > maxCapacity) {
            if (hasSystemPrompt && contextWindow.size() > 1) {
                // Evict the oldest standard message (index 1), protecting index 0 as 0 is the system prompt.
                contextWindow.remove(1);
            } else if (!hasSystemPrompt) {
                // Standard FIFO eviction
                contextWindow.removeFirst();
            } else {
                break;
            }
        }
    }


    public List<String> getContext() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(contextWindow);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return contextWindow.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}