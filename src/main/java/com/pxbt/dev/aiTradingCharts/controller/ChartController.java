package com.pxbt.dev.aiTradingCharts.controller;

import com.pxbt.dev.aiTradingCharts.model.PriceUpdate;
import com.pxbt.dev.aiTradingCharts.service.RealTimeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ChartController {

    @Autowired
    private RealTimeDataService dataService;

    @GetMapping("/")
    public String chartPage(Model model) {
        model.addAttribute("symbols", List.of("BTC", "SOL", "TAO", "WIF"));
        return "chart";
    }

    @GetMapping("/api/realtime/{symbol}")
    @ResponseBody
    public List<PriceUpdate> getRealTimeData(@PathVariable String symbol) {
        return dataService.getRecentPrices(symbol);
    }
}