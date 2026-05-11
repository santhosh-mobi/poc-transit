package com.example.paymentservice.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class TraceLogger {

    private static final String TRACE_LOG_PREFIX = "[TRACE-ROUTE]";

    public void logStep(String stepName, String details) {
        String requestId = MDC.get("requestId");
        log.info("{} [{}] {} : {}", TRACE_LOG_PREFIX, requestId, stepName, details);
    }

    public void logLatency(String operation, long durationMs) {
        String requestId = MDC.get("requestId");
        log.info("{} [{}] LATENCY | {} : {}ms", TRACE_LOG_PREFIX, requestId, operation, durationMs);
    }

    public void logResponse(String source, Object response) {
        String requestId = MDC.get("requestId");
        log.info("{} [{}] RESPONSE from {} : {}", TRACE_LOG_PREFIX, requestId, source, response);
    }

    public String detectRegion(String url) {
        if (url == null) return "Unknown";
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) return "Local/Internal";
            
            if (host.endsWith(".in")) return "India (AP-South-1)";
            if (host.endsWith(".us") || host.contains("us-east") || host.contains("us-west")) return "USA (US-East-1)";
            if (host.endsWith(".eu") || host.contains("eu-central")) return "Europe (EU-Central-1)";
            if (host.endsWith(".sg") || host.contains("ap-southeast")) return "Singapore (AP-Southeast-1)";
            if (host.contains("localhost") || host.contains("127.0.0.1")) return "Local Machine";
            
            return "Global (CDN/Anycast)";
        } catch (URISyntaxException e) {
            return "Invalid URL";
        }
    }
    
    public String buildVisualRoute(String... nodes) {
        return String.join(" ➔ ", nodes);
    }
}
