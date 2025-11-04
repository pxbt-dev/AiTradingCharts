package com.pxbt.dev.aiTradingCharts.dto;

import com.pxbt.dev.aiTradingCharts.model.CryptoPrice;
import com.pxbt.dev.aiTradingCharts.model.AIAnalysisResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ChartDataResponseDto {
    private List<CryptoPrice> prices;
    private AIAnalysisResult analysis; // Use existing AIAnalysisResult
    private String timeframe;

    public ChartDataResponseDto(List<CryptoPrice> prices, AIAnalysisResult analysis, String timeframe) {
        this.prices = prices;
        this.analysis = analysis;
        this.timeframe = timeframe;
    }
}