package com.example.paymentservice.client;

import com.example.paymentservice.util.TraceLogger;
import com.example.paymentservice.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class ExternalServiceClient {

    private final RestTemplate restTemplate;
    private final TraceLogger traceLogger;
    
    @Autowired
    public ExternalServiceClient(RestTemplate restTemplate, TraceLogger traceLogger) {
        this.restTemplate = restTemplate;
        this.traceLogger = traceLogger;
    }

    public Object postPayment(String externalServiceUrl, PaymentRequest request) {
        String region = traceLogger.detectRegion(externalServiceUrl);
        traceLogger.logStep("EXTERNAL_CALL_START", String.format("URL: %s | Target Region: %s", externalServiceUrl, region));
        
        long startTime = System.currentTimeMillis();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String traceId = MDC.get("requestId");
        if (traceId != null) {
            headers.add("X-Trace-Id", traceId);
        }
        
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            Object response = restTemplate.postForObject(externalServiceUrl, entity, Object.class);
            long latency = System.currentTimeMillis() - startTime;
            
            traceLogger.logLatency("EXTERNAL_API", latency);
            traceLogger.logResponse("EXTERNAL_SERVICE", response);
            return response;
        } catch (Exception e) {
            traceLogger.logStep("EXTERNAL_CALL_ERROR", e.getMessage());
            throw e;
        }
    }

    public Object executeDynamicRequest(String method, String url, Map<String, String> headersMap, Map<String, String> params, Object body) {
        String region = traceLogger.detectRegion(url);
        traceLogger.logStep("DYNAMIC_PROXY_START", String.format("%s %s | Target Region: %s", method, url, region));
        
        long startTime = System.currentTimeMillis();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        String finalUrl = builder.toUriString();

        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::add);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        String traceId = MDC.get("requestId");
        if (traceId != null && !headers.containsKey("X-Trace-Id")) {
            headers.add("X-Trace-Id", traceId);
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        HttpMethod httpMethod = HttpMethod.valueOf(method != null ? method.toUpperCase() : "POST");

        try {
            ResponseEntity<Object> responseEntity = restTemplate.exchange(finalUrl, httpMethod, entity, Object.class);
            long latency = System.currentTimeMillis() - startTime;
            
            traceLogger.logLatency("PROXY_EXTERNAL_API", latency);
            traceLogger.logResponse("PROXY_TARGET", responseEntity.getBody());
            
            return responseEntity.getBody();
        } catch (Exception e) {
            traceLogger.logStep("PROXY_CALL_ERROR", e.getMessage());
            throw e;
        }
    }
}
