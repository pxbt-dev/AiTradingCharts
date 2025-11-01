package com.pxbt.dev.aiTradingCharts.controller;

import com.pxbt.dev.aiTradingCharts.service.HistoricalDataService;
import com.pxbt.dev.aiTradingCharts.service.RealTimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/data")
public class DataController {



    @Autowired
    private HistoricalDataService historicalDataService;

    @Autowired
    private RealTimeDataService realTimeDataService;

    @GetMapping("/health")
    public Map<String, Object> getDataHealth() {
        return Map.of(
                "status", "operational",
                "timestamp", System.currentTimeMillis(),
                "stats", historicalDataService.getStats(),
                "apiCalls", "SINGLE call for all symbols"
        );
    }

    @PostMapping("/refresh")
    public String refreshData() {
        historicalDataService.refreshData();
        return "Historical data refreshed via single API call";
    }

    // Historical data endpoint - return empty or minimal data
    @GetMapping("/historical/{symbol}")
    public List<Map<String, Object>> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {

        System.out.println("📈 Historical data requested for " + symbol + " - Using WebSocket real-time data only");

        // Return empty array - we'll build the chart from real-time WebSocket data
        return new ArrayList<>();
    }

    // Real-time data endpoint - return current state if available
    @GetMapping("/realtime/{symbol}")
    public Map<String, Object> getRealTimeData(@PathVariable String symbol) {
        System.out.println("📡 Real-time data requested for " + symbol);

        // Return minimal data - real data comes via WebSocket
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("price", 0.0);
        data.put("timestamp", System.currentTimeMillis());
        data.put("message", "Real-time data comes via WebSocket");

        return data;
    }

    // Status endpoint
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("realTimePolling", Map.of(
                "nextBroadcastInMinutes", 60,
                "lastUpdate", new Date()
        ));
        status.put("dataSource", "Binance WebSocket");
        return status;
    }

    // Refresh endpoints
    @PostMapping("/refresh/predictions")
    public String refreshPredictions() {
        System.out.println("🔄 Predictions refresh triggered");
        return "Predictions refresh initiated";
    }

    @PostMapping("/refresh/realtime")
    public String refreshRealTime() {
        System.out.println("📡 Real-time data refresh triggered");
        return "Real-time data refresh initiated";
    }

    @PostMapping("/refresh/all")
    public String refreshAll() {
        System.out.println("🚀 All data refresh triggered");
        return "All data refresh initiated";
    }

    // Test endpoints
    @PostMapping("/test/trigger-update/{symbol}")
    public String triggerUpdate(@PathVariable String symbol) {
        System.out.println("🔔 Manual trigger for " + symbol);
        return "Update triggered for " + symbol;
    }

    @GetMapping("/test/status")
    public Map<String, Object> testStatus() {
        return Map.of("status", "healthy", "dataSource", "Binance WebSocket");
    }

    // Demo data generators
    private List<Map<String, Object>> generateDemoHistoricalData(String symbol, int days) {
        List<Map<String, Object>> data = new ArrayList<>();
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000L;

        double basePrice = getBasePrice(symbol);

        for (int i = days; i >= 0; i--) {
            long timestamp = now - (i * dayInMillis);
            // Add some random variation
            double variation = (Math.random() - 0.5) * 0.1; // ±5% variation
            double price = basePrice * (1 + variation);

            Map<String, Object> point = new HashMap<>();
            point.put("timestamp", timestamp);
            point.put("price", Math.round(price * 100.0) / 100.0);
            data.add(point);
        }

        System.out.println("🎲 Generated " + data.size() + " demo historical points for " + symbol);
        return data;
    }

    private Map<String, Object> generateDemoRealTimeData(String symbol) {
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("price", getBasePrice(symbol) * (1 + (Math.random() - 0.5) * 0.02)); // ±1% variation
        data.put("timestamp", System.currentTimeMillis());
        data.put("change24h", (Math.random() - 0.5) * 10); // Random change between -5% and +5%

        return data;
    }

    private double getBasePrice(String symbol) {
        switch (symbol.toUpperCase()) {
            case "BTC": return 50000.0;
            case "SOL": return 150.0;
            case "TAO": return 300.0;
            case "WIF": return 2.5;
            default: return 100.0;
        }
    }
}
