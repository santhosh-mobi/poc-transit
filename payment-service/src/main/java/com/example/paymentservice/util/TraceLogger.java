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
    
    private static final java.util.regex.Pattern IP_PATTERN = java.util.regex.Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");

    public java.util.List<String> getRealRoute(String url) {
        java.util.List<String> route = new java.util.ArrayList<>();
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (host == null) host = url;

            // Removed -d to enable hostname resolution. 
            // Limited to -h 5 to keep the response time somewhat reasonable.
            Process process = Runtime.getRuntime().exec("tracert -h 5 " + host);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // Regex to capture hostname and IP: "hostname [1.2.3.4]" or just "1.2.3.4"
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([a-zA-Z0-9.-]+)\\s*\\[?(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\]?").matcher(line);
                
                if (matcher.find()) {
                    String nameOrIp = matcher.group(1);
                    String ip = matcher.group(2);
                    
                    // If hostname and IP are the same, just show IP. Otherwise show both.
                    String display = nameOrIp.equalsIgnoreCase(ip) ? ip : nameOrIp + " (" + ip + ")";
                    
                    if (!route.contains(display)) {
                        route.add(display + " - " + detectRegionByIp(ip));
                    }
                }
            }
        } catch (Exception e) {
            route.add("Trace failed: " + e.getMessage());
        }
        return route;
    }

    private String detectRegionByIp(String ip) {
        if (ip.startsWith("192.168") || ip.startsWith("10.")) return "Local Network";
        if (ip.startsWith("14.141")) return "India VSNL";
        if (ip.startsWith("54.81")) return "AWS Virginia";
        if (ip.startsWith("13.228")) return "AWS Singapore";
        return "Public Gateway";
    }

    public String buildVisualRoute(String... nodes) {
        return String.join(" ➔ ", nodes);
    }
}
