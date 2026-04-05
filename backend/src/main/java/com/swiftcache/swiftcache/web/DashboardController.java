package com.swiftcache.swiftcache.web;
import com.swiftcache.swiftcache.core.MemoryEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final MemoryEngine engine;

    public DashboardController(MemoryEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/system")
    public Map<String, Object> getSystemState() {
        // Returns the total number of keys, plus the detailed breakdown of every agent's memory
        return Map.of(
                "status", "ONLINE",
                "totalKeys", engine.keyCount(),
                "memorySnapshot", engine.getSystemSnapshot(),
                "metrics", engine.getGlobalMetrics()
        );
    }
}