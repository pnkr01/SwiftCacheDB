package com.swiftcache.swiftcache.core;
import com.swiftcache.swiftcache.core.durability.PersistenceManager;
import com.swiftcache.swiftcache.core.gc.MemoryContainer;
import com.swiftcache.swiftcache.datastructures.ReasoningGraph;
import com.swiftcache.swiftcache.datastructures.SemanticList;
import com.swiftcache.swiftcache.enums.TaskStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MemoryEngine {

    // --- NEW: The map now holds our timestamp wrapper ---
    private final Map<String, MemoryContainer> dataStore;
    private final PersistenceManager persistenceManager;
    private final AtomicLong totalCommandsProcessed = new AtomicLong(0);

    // For testing: Evict memory if it hasn't been touched in 60 seconds
    private static final long IDLE_TIMEOUT_MS = 60*60*60 * 1000;

    public MemoryEngine(PersistenceManager persistenceManager) {
        this.dataStore = new ConcurrentHashMap<>();
        this.persistenceManager = persistenceManager;
    }

    // --- NEW: The Background Garbage Collector ---
    @PostConstruct
    public void startEvictionSweeper() {
        new Thread(() -> {
            System.out.println("🧹 Memory Sweeper Thread started.");
            while (true) {
                try {
                    // Wake up every 30 seconds to check for expired memory
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                long now = System.currentTimeMillis();
                int initialSize = dataStore.size();

                // Safely remove entries where the idle time exceeds our limit
                dataStore.entrySet().removeIf(entry -> {
                    boolean isIdle = (now - entry.getValue().getLastAccessedTime()) > IDLE_TIMEOUT_MS;
                    if (isIdle) {
                        System.out.println("🗑️ EVICTED: Agent memory for '" + entry.getKey() + "' was idle and removed to save RAM.");
                    }
                    return isIdle;
                });

                int evictedCount = initialSize - dataStore.size();
                if (evictedCount > 0) {
                    System.out.println("🧹 Sweeper finished: Freed " + evictedCount + " unused memory blocks.");
                    List<String> cleanState = generateRecreationCommands();
                    persistenceManager.rewriteAof(cleanState);
                }
            }
        }).start();
    }

    // --- Standard Methods (Updated for Container) ---
    public void set(String key, Object value) {
        dataStore.put(key, new MemoryContainer(value));
    }

    public Object get(String key) {
        MemoryContainer container = dataStore.get(key);
        return container != null ? container.getData() : null; // Unwraps and updates timestamp
    }

    public void incrementCommandCount() {
        totalCommandsProcessed.incrementAndGet();
    }

    public long getTotalCommands() {
        return totalCommandsProcessed.get();
    }

    public void delete(String key) { dataStore.remove(key); }
    public int keyCount() { return dataStore.size(); }

    // --- AI-Aware Methods (Updated for Container) ---

    public void pushToContext(String key, String message, int maxCapacity, boolean hasSystemPrompt) {
        // computeIfAbsent now creates a wrapped SemanticList
        this.incrementCommandCount();
        MemoryContainer container = dataStore.computeIfAbsent(key, k -> new MemoryContainer(new SemanticList(maxCapacity, hasSystemPrompt)));

        Object storedData = container.getData(); // This updates the timestamp!
        if (storedData instanceof SemanticList) {
            ((SemanticList) storedData).push(message);
        } else {
            throw new IllegalArgumentException("Key '" + key + "' is not a SemanticList type.");
        }
    }

    public List<String> getContext(String key) {
        this.incrementCommandCount();
        Object storedData = get(key);
        if (storedData instanceof SemanticList) {
            return ((SemanticList) storedData).getContext();
        }
        return null;
    }

    public void addGraphTask(String key, String taskId, String description) {
        this.incrementCommandCount();
        MemoryContainer container = dataStore.computeIfAbsent(key, k -> new MemoryContainer(new ReasoningGraph()));
        Object storedData = container.getData();
        if (storedData instanceof ReasoningGraph) {
            ((ReasoningGraph) storedData).addTask(taskId, description);
        }
    }

    public void updateGraphTask(String key, String taskId, TaskStatus status) {
        this.incrementCommandCount();
        Object storedData = get(key);
        if (storedData instanceof ReasoningGraph) {
            ((ReasoningGraph) storedData).updateTaskStatus(taskId, status);
        }
    }

    // --- Dashboard Snapshot (Updated for Container) ---
    public Map<String, Object> getSystemSnapshot() {

        Map<String, Object> snapshot = new java.util.HashMap<>();
        dataStore.forEach((key, container) -> {
            Object value = container.peekData();

            if (value instanceof SemanticList) {
                snapshot.put(key, Map.of("type", "Context Window", "messageCount", ((SemanticList) value).size()));
            } else if (value instanceof ReasoningGraph) {
                snapshot.put(key, Map.of("type", "Reasoning Graph", "tasks", ((ReasoningGraph) value).getGraphState()));
            }
        });
        return snapshot;
    }

    // Translates the current RAM state into a clean, optimized list of commands.
    public List<String> generateRecreationCommands() {
        List<String> commands = new ArrayList<>();

        dataStore.forEach((key, container) -> {
            Object value = container.peekData(); // Use peek so we don't reset the idle timer!

            if (value instanceof SemanticList) {
                // Recreate the chat history
                for (String msg : ((SemanticList) value).getContext()) {
                    commands.add("PUSH " + key + " " + msg);
                }
            } else if (value instanceof ReasoningGraph) {
                // Recreate the graph nodes and their statuses
                for (ReasoningGraph.TaskNode node : ((ReasoningGraph) value).getTasks()) {
                    commands.add("GRAPH_ADD " + key + " " + node.id + " " + node.description);
                    // Only add an update command if it's not PENDING (which is the default)
                    if (node.status != TaskStatus.PENDING) {
                        commands.add("GRAPH_UPDATE " + key + " " + node.id + " " + node.status.name());
                    }
                }
            }
        });

        return commands;
    }

    public Map<String, Object> getGlobalMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        Runtime runtime = Runtime.getRuntime();

        return Map.of(
                "cpuUsage", String.format("%.2f%%", osBean.getSystemCpuLoad() * 100),
                "usedHeap", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB",
                "maxHeap", runtime.maxMemory() / (1024 * 1024) + " MB",
                "threads", Thread.activeCount(),
                "totalCommands", getTotalCommands()
        );
    }
}