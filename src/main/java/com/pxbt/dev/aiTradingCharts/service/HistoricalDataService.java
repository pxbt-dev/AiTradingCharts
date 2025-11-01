package com.pxbt.dev.aiTradingCharts.service;

import com.pxbt.dev.aiTradingCharts.model.CryptoPrice;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HistoricalDataService {
    private static final Logger logger = LoggerFactory.getLogger(HistoricalDataService.class);

    @Autowired
    private CoinGeckoService coinGeckoService;

    private Map<String, List<CryptoPrice>> currentData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("📚 HistoricalDataService initializing with SINGLE API call...");
        loadAllDataInOneCall();
    }

    private void loadAllDataInOneCall() {
        try {
            // ONE API CALL for all symbols
            Map<String, List<CryptoPrice>> allData = coinGeckoService.getAllHistoricalData(7); // 7 days is enough

            currentData.putAll(allData);

            // Log results
            for (Map.Entry<String, List<CryptoPrice>> entry : allData.entrySet()) {
                logger.info("✅ {}: {} data points", entry.getKey(), entry.getValue().size());
            }

            logger.info("🎉 All historical data loaded in ONE API call");

        } catch (Exception e) {
            logger.error("❌ Failed to load historical data: {}", e.getMessage());
            // Initialize with empty data
            currentData = Map.of(
                    "BTC", new ArrayList<>(),
                    "SOL", new ArrayList<>(),
                    "TAO", new ArrayList<>(),
                    "WIF", new ArrayList<>()
            );
        }
    }

    public List<CryptoPrice> getHistoricalData(String symbol, int days) {
        List<CryptoPrice> data = currentData.get(symbol);
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        // Return requested number of days (or all available)
        int pointsNeeded = Math.min(days, data.size());
        int startIndex = Math.max(0, data.size() - pointsNeeded);
        return new ArrayList<>(data.subList(startIndex, data.size()));
    }

    public List<CryptoPrice> getFullHistoricalData(String symbol) {
        return currentData.getOrDefault(symbol, new ArrayList<>());
    }

    public void refreshData() {
        logger.info("🔄 Refreshing all historical data...");
        coinGeckoService.refreshCache();
        loadAllDataInOneCall();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("loadedSymbols", currentData.keySet());

        Map<String, Integer> dataPoints = new HashMap<>();
        for (Map.Entry<String, List<CryptoPrice>> entry : currentData.entrySet()) {
            dataPoints.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("dataPoints", dataPoints);

        return stats;
    }
}