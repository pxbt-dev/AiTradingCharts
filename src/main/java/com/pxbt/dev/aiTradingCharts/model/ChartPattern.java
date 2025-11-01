package com.pxbt.dev.aiTradingCharts.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartPattern {
    private String symbol;
    private String patternType;
    private double priceLevel;
    private double confidence;
    private String description;
    private long timestamp;

    public ChartPattern(String doubleBottom, double price, double v, String bullishReversalPattern, long currentTimestamp) {
    }

    // Custom methods
    public String getFormattedConfidence() {
        return String.format("%.0f%%", confidence * 100);
    }

    public String getTrendDirection() {
        switch (patternType) {
            case "UPTREND":
            case "BULLISH_ENGULFING":
            case "DOUBLE_BOTTOM":
                return "BULLISH";
            case "DOWNTREND":
            case "BEARISH_ENGULFING":
            case "DOUBLE_TOP":
            case "HEAD_SHOULDERS":
                return "BEARISH";
            default:
                return "NEUTRAL";
        }
    }
}