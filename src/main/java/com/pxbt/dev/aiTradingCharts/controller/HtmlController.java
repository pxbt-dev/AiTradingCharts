package com.pxbt.dev.aiTradingCharts.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HtmlController {

    @GetMapping("/")
    public String chartPage(Model model) {
        System.out.println("🎯 Serving main chart page");
        model.addAttribute("symbols", List.of("BTC", "SOL", "TAO", "WIF"));
        return "chart"; // This serves chart.html from templates/
    }
}