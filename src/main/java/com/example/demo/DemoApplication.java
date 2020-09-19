package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class DemoApplication implements CommandLineRunner {
    private static final Logger resourcesConsumption = LoggerFactory.getLogger("resources-consumption");
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Scheduled(fixedRate = 200)
    public void logResourcesConsumption() {
        resourcesConsumption.info("{};{};{}", System.currentTimeMillis(), getCpuUsage(), getMemoryUsage());
    }

    @Override
    public void run(String... args) throws Exception {
        resourcesConsumption.info("timestamp;cpu;memory");
    }

    private Double getCpuUsage() {
        Map<String, Object> response = restTemplate.getForObject("http://localhost:8080/actuator/metrics/system.cpu.usage", Map.class);
        Double cpuUsage = (Double) ((List<Map<String, Object>>) response.get("measurements")).get(0).get("value");
        return cpuUsage;
    }

    private Long getMemoryUsage() {
        Map<String, Object> response = restTemplate.getForObject("http://localhost:8080/actuator/metrics/jvm.memory.used", Map.class);
        Double cpuUsageDouble = (Double) ((List<Map<String, Object>>) response.get("measurements")).get(0).get("value");
        return cpuUsageDouble.longValue();
    }
}
