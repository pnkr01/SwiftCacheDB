package com.swiftcache.swiftcache.core.durability;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class PersistenceManager {
    // The active state file (gets compacted)
    private static final Path AOF_PATH = Paths.get("agentmemory-aof.log");
    // The permanent history file (never deleted)
    private static final Path AUDIT_PATH = Paths.get("agentmemory-audit.log");

    public PersistenceManager() {
        try {
            if (!Files.exists(AOF_PATH)) Files.createFile(AOF_PATH);
            if (!Files.exists(AUDIT_PATH)) Files.createFile(AUDIT_PATH);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to initialize log files: " + e.getMessage());
        }
    }


    public synchronized void appendCommand(String command) {
        try {
            String logEntry = command + System.lineSeparator();
            Files.writeString(AOF_PATH, logEntry, StandardOpenOption.APPEND);
            Files.writeString(AUDIT_PATH, logEntry, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to write to logs: " + e.getMessage());
        }
    }

    public synchronized void rewriteAof(List<String> cleanCommands) {
        try {
            // Files.write completely overwrites the file by default
            Files.write(AOF_PATH, cleanCommands);
            System.out.println("💾 AOF File successfully compacted (Audit log remains untouched).");
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to rewrite AOF: " + e.getMessage());
        }
    }

    //Reads the entire log file to replay on startup.
    public List<String> readAllCommands() {
        try {
            return Files.readAllLines(AOF_PATH);
        } catch (IOException e) {
            System.err.println("WARNING: Could not read AOF file: " + e.getMessage());
            return List.of();
        }
    }

    // Writes a fresh, optimized snapshot to disk and replaces the old bloated AOF file.
    public synchronized void compactAof(List<String> optimizedCommands) {
        try {
            System.out.println("📦 Starting AOF Compaction...");
            Path tempPath = Paths.get("swiftCache-aof.tmp");

            // 1. Write the clean, optimized commands to a temporary file
            Files.write(tempPath, optimizedCommands, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 2. Safely swap the temp file with the real file
            Files.move(tempPath, AOF_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ AOF Compaction Complete! Log optimized to " + optimizedCommands.size() + " commands.");
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to compact AOF file: " + e.getMessage());
        }
    }
}