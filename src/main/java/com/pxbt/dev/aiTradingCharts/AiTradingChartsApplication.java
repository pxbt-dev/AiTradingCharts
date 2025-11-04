package com.pxbt.dev.aiTradingCharts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class AiTradingChartsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiTradingChartsApplication.class, args);
	}

	@PostConstruct
	public void startMemoryMonitoring() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Runtime runtime = Runtime.getRuntime();
				long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
				long maxMemory = runtime.maxMemory() / (1024 * 1024);
				long freeMemory = runtime.freeMemory() / (1024 * 1024);

				System.out.println("🧠 MEMORY USAGE: " + usedMemory + "MB used / " + maxMemory + "MB max / " + freeMemory + "MB free");
			}
		}, 10000, 30000); // Start after 10 seconds, repeat every 30 seconds
	}
}