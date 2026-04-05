package com.swiftcache.swiftcache.datastructures;

import com.swiftcache.swiftcache.enums.TaskStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReasoningGraph {

    // Represents a single step in the agent's reasoning/execution plan
    public static class TaskNode {
        public String id;
        public String description;
        public TaskStatus status;

        public TaskNode(String id, String description) {
            this.id = id;
            this.description = description;
            this.status = TaskStatus.PENDING;
        }
    }

    // Stores all nodes by their ID. Thread-safe.
    private final Map<String, TaskNode> nodes = new ConcurrentHashMap<>();

    // Stores dependencies.
    private final Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();

    //Adds a new task to the scratchpad.
    public void addTask(String id, String description) {
        nodes.putIfAbsent(id, new TaskNode(id, description));
    }

    //An agent calls this to update its progress.
    public void updateTaskStatus(String id, TaskStatus newStatus) {
        TaskNode node = nodes.get(id);
        if (node != null) {
            // Because nodes are objects, we synchronize on the specific node to update it safely,
            // leaving the rest of the graph completely unblocked for other agents.
            synchronized (node) {
                node.status = newStatus;
            }
        } else {
            throw new IllegalArgumentException("Task ID " + id + " does not exist.");
        }
    }

    // View the current state of the graph.
    public Map<String, TaskStatus> getGraphState() {
        Map<String, TaskStatus> state = new ConcurrentHashMap<>();
        nodes.forEach((key, node) -> state.put(key, node.status));
        return state;
    }

    // Returns all tasks so we can reconstruct the graph later
    public Collection<TaskNode> getTasks() {
        return nodes.values();
    }
}