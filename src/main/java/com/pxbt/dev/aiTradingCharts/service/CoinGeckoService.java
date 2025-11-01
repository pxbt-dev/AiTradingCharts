package com.pxbt.dev.aiTradingCharts.service;

import com.pxbt.dev.aiTradingCharts.model.CryptoPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CoinGeckoService {
    private static final Logger logger = LoggerFactory.getLogger(CoinGeckoService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Cache
    private final Map<String, List<CryptoPrice>> historicalCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 60 * 60 * 1000; // 1 hour cache

    private long lastBatchRequestTime = 0;
    private static final long BATCH_REQUEST_INTERVAL = 30000; // 30 seconds between batch requests

    public CoinGeckoService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get ALL historical data in ONE API call
     */
    public Map<String, List<CryptoPrice>> getAllHistoricalData(int days) {
        // Check if we recently made a batch request
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBatchRequestTime < BATCH_REQUEST_INTERVAL) {
            logger.debug("✅ Using cached batch data (within 30s window)");
            return historicalCache.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        try {
            logger.info("🌐 Fetching ALL historical data ({} days) in ONE API call...", days);

            // Single API call for all coins
            String url = String.format(
                    "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&days=%d&interval=daily&ids=bitcoin,solana,bittensor,dogwifcoin&price_change_percentage=24h",
                    days
            );

            String jsonResponse = makeApiRequest(url);
            Map<String, List<CryptoPrice>> allData = parseBatchResponse(jsonResponse, days);

            // Update cache and timestamp
            historicalCache.putAll(allData);
            lastBatchRequestTime = System.currentTimeMillis();

            logger.info("✅ Batch fetch complete: {}", allData.keySet());
            return allData;

        } catch (Exception e) {
            logger.error("❌ Batch API failed: {} - using cached data", e.getMessage());
            return getCachedFallback();
        }
    }

    private String makeApiRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "CryptoAI-Trading/1.0")
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Rate limit exceeded");
        } else if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    private Map<String, List<CryptoPrice>> parseBatchResponse(String jsonResponse, int days) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        Map<String, List<CryptoPrice>> allData = new HashMap<>();

        // Map CoinGecko IDs to our symbols
        Map<String, String> idToSymbol = Map.of(
                "bitcoin", "BTC",
                "solana", "SOL",
                "bittensor", "TAO",
                "dogwifcoin", "WIF"
        );

        for (JsonNode coinData : root) {
            String coinId = coinData.get("id").asText();
            String symbol = idToSymbol.get(coinId);

            if (symbol != null && coinData.has("sparkline_in_7d")) {
                JsonNode sparkline = coinData.get("sparkline_in_7d").get("price");
                List<CryptoPrice> prices = new ArrayList<>();

                // Generate timestamps (last 7 days)
                long now = System.currentTimeMillis();
                long dayInMillis = 24 * 60 * 60 * 1000L;

                for (int i = 0; i < sparkline.size(); i++) {
                    double price = sparkline.get(i).asDouble();
                    long timestamp = now - ((sparkline.size() - 1 - i) * dayInMillis);
                    prices.add(new CryptoPrice(symbol, price, 0, timestamp)); // Volume not available in batch
                }

                allData.put(symbol, prices);
                logger.debug("📊 Processed {} data points for {}", prices.size(), symbol);
            }
        }

        return allData;
    }

    private Map<String, List<CryptoPrice>> getCachedFallback() {
        if (!historicalCache.isEmpty()) {
            logger.warn("⚠️ Using stale cached data (API request failed)");
            return new HashMap<>(historicalCache);
        }

        logger.warn("⚠️ No cached data available, returning empty");
        return Map.of(
                "BTC", new ArrayList<>(),
                "SOL", new ArrayList<>(),
                "TAO", new ArrayList<>(),
                "WIF", new ArrayList<>()
        );
    }

    /**
     * Get individual symbol data (uses batch internally)
     */
    public List<CryptoPrice> getHistoricalData(String symbol, int days) {
        Map<String, List<CryptoPrice>> allData = getAllHistoricalData(days);
        return allData.getOrDefault(symbol, new ArrayList<>());
    }

    public void refreshCache() {
        historicalCache.clear();
        lastBatchRequestTime = 0;
        logger.info("🔄 Cache cleared - next request will fetch fresh data");
    }
}