package com.pxbt.dev.aiTradingCharts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    private String symbol;
    private double currentPrice;
    private Map<String, PricePrediction> timeframePredictions;
    private List<ChartPattern> chartPatterns;
    private List<FibonacciTimeZone> fibonacciTimeZones; // NEW: Fibonacci zones
    private long timestamp;

    // Your existing custom methods
    public PricePrediction getMainPrediction() {
        return timeframePredictions != null ? timeframePredictions.get("1day") : null;
    }

    public double getPredictedPrice() {
        PricePrediction main = getMainPrediction();
        return main != null ? main.getPredictedPrice() : currentPrice;
    }

    public double getConfidence() {
        PricePrediction main = getMainPrediction();
        return main != null ? main.getConfidence() : 0.1;
    }

    public String getTradingSignal() {
        PricePrediction main = getMainPrediction();
        return main != null ? generateTradingSignal(main) : "HOLD";
    }

    private String generateTradingSignal(PricePrediction prediction) {
        double changePercent = ((prediction.getPredictedPrice() - currentPrice) / currentPrice) * 100;
        double confidence = prediction.getConfidence();

        if (changePercent > 2.0 && confidence > 0.7) return "STRONG_BUY";
        if (changePercent > 0.5 && confidence > 0.6) return "BUY";
        if (changePercent < -2.0 && confidence > 0.7) return "STRONG_SELL";
        if (changePercent < -0.5 && confidence > 0.6) return "SELL";
        return "HOLD";
    }
}