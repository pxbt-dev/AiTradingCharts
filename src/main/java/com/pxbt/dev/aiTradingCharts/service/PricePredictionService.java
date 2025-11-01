package com.pxbt.dev.aiTradingCharts.service;

import com.pxbt.dev.aiTradingCharts.model.CryptoPrice;
import com.pxbt.dev.aiTradingCharts.model.PricePrediction;
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PricePredictionService {
    private static final Logger logger = LoggerFactory.getLogger(PricePredictionService.class);

    @Autowired
    private HistoricalDataService historicalDataService;

    private static final int LOOKBACK_PERIOD = 50;

    /**
     * Original single prediction method (for backward compatibility)
     */
    public PricePrediction predictNextPrice(String symbol, List<CryptoPrice> recentPrices) {
        if (recentPrices == null || recentPrices.size() < LOOKBACK_PERIOD) {
            return createDefaultPrediction(symbol, recentPrices);
        }

        // Prepare features for AI
        double[] features = extractFeatures(recentPrices);

        // Multiple prediction models
        double lstmPrediction = lstmPredict(features);
        double arimaPrediction = arimaPredict(features);
        double ensemblePrediction = ensemblePredict(lstmPrediction, arimaPrediction);

        // Calculate confidence
        double confidence = calculatePredictionConfidence(features, recentPrices);

        // Determine trend
        String trend = determineTrend(ensemblePrediction, getCurrentPrice(recentPrices));

        return new PricePrediction(
                symbol,
                ensemblePrediction,
                confidence,
                trend
        );
    }

    /**
     * NEW: Predict multiple timeframes (1 day, 3 days, 5 days, 7 days, 30 days)
     */
    public Map<String, PricePrediction> predictMultipleTimeframes(String symbol, double currentPrice) {
        Map<String, PricePrediction> predictions = new LinkedHashMap<>();

        try {
            // Get historical data for different analyses
            List<CryptoPrice> shortTermData = historicalDataService.getHistoricalData(symbol, 30); // 30 days
            List<CryptoPrice> mediumTermData = historicalDataService.getHistoricalData(symbol, 90); // 90 days
            List<CryptoPrice> longTermData = historicalDataService.getFullHistoricalData(symbol);

            // Generate predictions for different timeframes
            predictions.put("1day", predictForTimeframe(symbol, currentPrice, shortTermData, 1, "SHORT_TERM"));
            predictions.put("3days", predictForTimeframe(symbol, currentPrice, mediumTermData, 3, "SHORT_TERM"));
            predictions.put("5days", predictForTimeframe(symbol, currentPrice, mediumTermData, 5, "MEDIUM_TERM"));
            predictions.put("7days", predictForTimeframe(symbol, currentPrice, mediumTermData, 7, "MEDIUM_TERM"));
            predictions.put("30days", predictForTimeframe(symbol, currentPrice, longTermData, 30, "LONG_TERM"));

            logger.debug("📊 Generated {} timeframe predictions for {}", predictions.size(), symbol);

        } catch (Exception e) {
            logger.error("❌ Multi-timeframe prediction failed for {}: {}", symbol, e.getMessage());
            // Return default predictions on error
            predictions.put("1day", createDefaultPrediction(symbol, currentPrice, 1));
            predictions.put("3days", createDefaultPrediction(symbol, currentPrice, 3));
            predictions.put("5days", createDefaultPrediction(symbol, currentPrice, 5));
            predictions.put("7days", createDefaultPrediction(symbol, currentPrice, 7));
            predictions.put("30days", createDefaultPrediction(symbol, currentPrice, 30));
        }

        return predictions;
    }

    /**
     * Predict for a specific timeframe
     */
    private PricePrediction predictForTimeframe(String symbol, double currentPrice,
                                                List<CryptoPrice> historicalData,
                                                int days, String timeframeType) {
        if (historicalData.isEmpty()) {
            return createDefaultPrediction(symbol, currentPrice, days);
        }

        try {
            double[] features = extractAdvancedFeatures(historicalData, timeframeType);
            double predictedPrice = calculateTimeframePrediction(currentPrice, features, days, timeframeType);
            double confidence = calculateTimeframeConfidence(features, historicalData, days);
            String trend = determineTrend(predictedPrice, currentPrice);

            Map<String, Double> priceTargets = calculatePriceTargets(predictedPrice, confidence);
            Map<String, String> timeHorizons = Map.of(
                    "timeframe", days + " days",
                    "type", timeframeType
            );

            return new PricePrediction(
                    symbol,
                    predictedPrice,
                    confidence,
                    trend,
                    priceTargets,
                    timeHorizons
            );

        } catch (Exception e) {
            logger.error("❌ Error in {} prediction for {}: {}", days + "day", symbol, e.getMessage());
            return createDefaultPrediction(symbol, currentPrice, days);
        }
    }

    private double[] extractAdvancedFeatures(List<CryptoPrice> data, String timeframeType) {
        double[] prices = data.stream().mapToDouble(CryptoPrice::getPrice).toArray();

        if (timeframeType.equals("SHORT_TERM")) {
            return new double[] {
                    calculateSMA(prices, 5), calculateSMA(prices, 20),
                    calculateRSI(prices, 14), calculateMACD(prices),
                    calculateVolatility(prices, 10), calculateMomentum(prices, 5),
                    calculateVolumeTrend(data), calculatePriceAcceleration(prices)
            };
        } else if (timeframeType.equals("MEDIUM_TERM")) {
            return new double[] {
                    calculateSMA(prices, 20), calculateSMA(prices, 50),
                    calculateRSI(prices, 21), calculateVolatility(prices, 20),
                    calculateTrendStrength(prices), calculateSupportResistance(prices),
                    calculateSeasonality(data), calculateMarketCycle(data)
            };
        } else { // LONG_TERM
            return new double[] {
                    calculateSMA(prices, 50), calculateSMA(prices, 200),
                    calculateVolatility(prices, 60), calculateLongTermTrend(prices),
                    calculateMarketMaturity(data), calculateAdoptionMetrics(data)
            };
        }
    }

    private double calculateTimeframePrediction(double currentPrice, double[] features,
                                                int days, String timeframeType) {
        double basePrediction = currentPrice;

        // Different models for different timeframes
        switch (timeframeType) {
            case "SHORT_TERM":
                // Technical analysis + momentum based
                double momentum = features[5]; // momentum feature
                double volatility = features[4];
                basePrediction += (momentum * currentPrice * 0.01 * days);
                break;

            case "MEDIUM_TERM":
                // Trend + cycle based
                double trendStrength = features[4];
                double seasonality = features[6];
                basePrediction += (trendStrength * currentPrice * 0.005 * days);
                basePrediction += (seasonality * currentPrice * 0.002 * days);
                break;

            case "LONG_TERM":
                // Fundamental + macro based
                double longTermTrend = features[3];
                double marketMaturity = features[4];
                basePrediction += (longTermTrend * currentPrice * 0.001 * days);
                basePrediction *= (1 + marketMaturity * 0.1);
                break;
        }

        return Math.max(0.01, basePrediction); // Ensure positive price
    }

    private double calculateTimeframeConfidence(double[] features, List<CryptoPrice> data, int days) {
        double baseConfidence = 0.5;

        // Confidence decreases with longer timeframes
        double timeframeFactor = 1.0 / Math.log(days + 1);

        // More data points = higher confidence
        double dataFactor = Math.min(1.0, data.size() / (days * 2.0));

        // Lower volatility = higher confidence
        double volatility = features[4];
        double volatilityFactor = Math.max(0, 1 - (volatility * 5));

        double confidence = baseConfidence * timeframeFactor * dataFactor * volatilityFactor;
        return Math.max(0.1, Math.min(0.9, confidence));
    }

    // ===== ORIGINAL PREDICTION METHODS =====

    private PricePrediction createDefaultPrediction(String symbol, List<CryptoPrice> recentPrices) {
        double currentPrice = getCurrentPrice(recentPrices);
        return new PricePrediction(
                symbol,
                currentPrice, // No change prediction
                0.1, // Low confidence
                "NEUTRAL"
        );
    }

    private PricePrediction createDefaultPrediction(String symbol, double currentPrice, int days) {
        return new PricePrediction(
                symbol,
                currentPrice,
                0.1, // Low confidence
                "NEUTRAL"
        );
    }

    private double getCurrentPrice(List<CryptoPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return 45000.0; // Default BTC price
        }
        return prices.get(prices.size() - 1).getPrice();
    }

    private double[] extractFeatures(List<CryptoPrice> prices) {
        double[] priceArray = prices.stream()
                .mapToDouble(CryptoPrice::getPrice)
                .toArray();

        return new double[] {
                // Technical Indicators
                calculateSMA(priceArray, 5),
                calculateSMA(priceArray, 20),
                calculateEMA(priceArray, 12),
                calculateEMA(priceArray, 26),
                calculateRSI(priceArray, 14),
                calculateMACD(priceArray),
                calculateVolatility(priceArray, 20),

                // Price Action
                calculateMomentum(priceArray, 10),
                calculatePriceRateOfChange(priceArray, 10),

                // Volume Analysis (simplified)
                calculateVolumeStrength(prices),

                // Statistical Features
                calculateZScore(priceArray)
        };
    }

    private double lstmPredict(double[] features) {
        // Simplified LSTM-like prediction
        double prediction = 0;
        double[] weights = generateDynamicWeights(features);

        for (int i = 0; i < features.length; i++) {
            prediction += features[i] * weights[i];
        }

        double lastPrice = features[0];
        double trendComponent = calculateTrendStrength(features) * lastPrice * 0.01;

        return lastPrice + prediction + trendComponent;
    }

    private double arimaPredict(double[] features) {
        double[] prices = extractPriceSeries(features);

        if (prices.length < 10) return prices[prices.length - 1];

        // Simple AR model
        double alpha = 0.1;
        double beta1 = 0.4;
        double beta2 = 0.3;
        double beta3 = 0.2;

        int n = prices.length;
        return alpha +
                beta1 * prices[n-1] +
                beta2 * prices[n-2] +
                beta3 * prices[n-3];
    }

    private double ensemblePredict(double lstmPred, double arimaPred) {
        double lstmWeight = 0.6;
        double arimaWeight = 0.4;
        return (lstmPred * lstmWeight) + (arimaPred * arimaWeight);
    }

    private double calculatePredictionConfidence(double[] features, List<CryptoPrice> prices) {
        double volatility = calculateVolatility(extractPriceSeries(features), 10);
        double trendStrength = calculateTrendStrength(features);
        double rsi = features[4];

        double baseConfidence = 0.5;
        double volAdjustment = Math.max(0, 1 - (volatility * 10));
        double trendAdjustment = trendStrength;
        double rsiAdjustment = 1 - Math.abs(rsi - 50) / 50;

        double rawConfidence = baseConfidence +
                (volAdjustment * 0.2) +
                (trendAdjustment * 0.15) +
                (rsiAdjustment * 0.1);

        return Math.max(0.1, Math.min(0.95, rawConfidence));
    }

    private String determineTrend(double predictedPrice, double currentPrice) {
        double change = ((predictedPrice - currentPrice) / currentPrice) * 100;

        if (change > 2.0) return "STRONG_BULLISH";
        if (change > 0.5) return "BULLISH";
        if (change > -0.5) return "NEUTRAL";
        if (change > -2.0) return "BEARISH";
        return "STRONG_BEARISH";
    }

    private Map<String, Double> calculatePriceTargets(double predictedPrice, double confidence) {
        Map<String, Double> targets = new HashMap<>();
        targets.put("conservative", predictedPrice * 0.7 + (predictedPrice * 0.3 * confidence));
        targets.put("expected", predictedPrice);
        targets.put("optimistic", predictedPrice * 1.3 * confidence);
        return targets;
    }

    // ===== TECHNICAL INDICATORS =====

    private double calculateSMA(double[] prices, int period) {
        if (prices.length < period) return prices[prices.length-1];
        double sum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }

    private double calculateEMA(double[] prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = prices[0];
        for (int i = 1; i < prices.length; i++) {
            ema = (prices[i] * multiplier) + (ema * (1 - multiplier));
        }
        return ema;
    }

    private double calculateRSI(double[] prices, int period) {
        double gains = 0, losses = 0;
        for (int i = 1; i <= period; i++) {
            double change = prices[prices.length - i] - prices[prices.length - i - 1];
            if (change > 0) gains += change;
            else losses -= change;
        }
        double avgGain = gains / period;
        double avgLoss = losses / period;
        double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private double calculateMACD(double[] prices) {
        double ema12 = calculateEMA(prices, 12);
        double ema26 = calculateEMA(prices, 26);
        return ema12 - ema26;
    }

    private double calculateVolatility(double[] prices, int period) {
        double mean = calculateSMA(prices, period);
        double sum = 0;
        int start = Math.max(0, prices.length - period);
        for (int i = start; i < prices.length; i++) {
            sum += Math.pow(prices[i] - mean, 2);
        }
        return Math.sqrt(sum / Math.min(period, prices.length - start));
    }

    private double calculateMomentum(double[] prices, int period) {
        if (prices.length < period) return 0;
        return prices[prices.length-1] - prices[prices.length-period];
    }

    private double calculatePriceRateOfChange(double[] prices, int period) {
        if (prices.length < period) return 0;
        return ((prices[prices.length-1] - prices[prices.length-period]) / prices[prices.length-period]) * 100;
    }

    private double calculateVolumeStrength(List<CryptoPrice> prices) {
        if (prices.size() < 2) return 0.5;
        double currentVolume = prices.get(prices.size()-1).getVolume();
        double avgVolume = prices.stream()
                .mapToDouble(CryptoPrice::getVolume)
                .average()
                .orElse(1.0);
        return currentVolume / avgVolume;
    }

    private double calculateZScore(double[] prices) {
        if (prices.length < 2) return 0;
        double mean = calculateSMA(prices, prices.length);
        double stdDev = calculateVolatility(prices, prices.length);
        return stdDev == 0 ? 0 : (prices[prices.length-1] - mean) / stdDev;
    }

    // ===== ADVANCED FEATURE METHODS =====

    private double calculateTrendStrength(double[] prices) {
        if (prices.length < 10) return 0;
        double sma20 = calculateSMA(prices, Math.min(20, prices.length));
        double sma50 = calculateSMA(prices, Math.min(50, prices.length));
        return (sma20 - sma50) / sma50;
    }

    private double calculateSupportResistance(double[] prices) {
        if (prices.length < 10) return 0;
        double current = prices[prices.length - 1];
        double avg = calculateSMA(prices, prices.length);
        return (current - avg) / avg;
    }

    private double calculateSeasonality(List<CryptoPrice> data) {
        if (data.size() < 7) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(data.get(data.size() - 1).getTimestamp());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek >= 2 && dayOfWeek <= 5) ? 0.1 : -0.1;
    }

    private double calculateMarketCycle(List<CryptoPrice> data) {
        if (data.size() < 30) return 0;
        double[] prices = data.stream().mapToDouble(CryptoPrice::getPrice).toArray();
        double momentum30 = calculateMomentum(prices, 30);
        double momentum10 = calculateMomentum(prices, 10);
        return (momentum30 - momentum10) / Math.abs(momentum30);
    }

    private double calculateLongTermTrend(double[] prices) {
        if (prices.length < 100) return 0;
        // Simple linear regression slope
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = prices.length;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += prices[i];
            sumXY += i * prices[i];
            sumX2 += i * i;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return slope / prices[0]; // Normalized slope
    }

    private double calculateMarketMaturity(List<CryptoPrice> data) {
        if (data.size() < 60) return 0.1;
        double volatility = calculateVolatility(
                data.stream().mapToDouble(CryptoPrice::getPrice).toArray(),
                Math.min(60, data.size())
        );
        return Math.max(0, 1 - volatility * 10);
    }

    private double calculateAdoptionMetrics(List<CryptoPrice> data) {
        return data.size() > 180 ? 0.05 : 0.02;
    }

    private double calculatePriceAcceleration(double[] prices) {
        if (prices.length < 3) return 0;
        double change1 = (prices[prices.length-1] - prices[prices.length-2]) / prices[prices.length-2];
        double change2 = (prices[prices.length-2] - prices[prices.length-3]) / prices[prices.length-3];
        return change1 - change2;
    }

    private double calculateVolumeTrend(List<CryptoPrice> data) {
        if (data.size() < 5) return 0.5;
        double currentVolume = data.get(data.size()-1).getVolume();
        double avgVolume = data.stream()
                .limit(data.size()-1)
                .mapToDouble(CryptoPrice::getVolume)
                .average()
                .orElse(1.0);
        return currentVolume / avgVolume;
    }

    // ===== UTILITY METHODS =====

    private double[] generateDynamicWeights(double[] features) {
        double[] weights = new double[features.length];
        double volatility = calculateVolatility(extractPriceSeries(features), 10);

        for (int i = 0; i < weights.length; i++) {
            weights[i] = (Math.random() * 0.2 - 0.1) * (1 - volatility);
        }
        return weights;
    }

    private double[] extractPriceSeries(double[] features) {
        // Extract price-like values from features (simplified)
        double[] prices = new double[10];
        Arrays.fill(prices, features[0]);
        return prices;
    }
}