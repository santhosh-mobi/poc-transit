package com.example.paymentservice.service;

import com.example.paymentservice.client.ExternalServiceClient;
import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.ProxyRequest;
import com.example.paymentservice.util.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    private final ExternalServiceClient externalServiceClient;
    private final TraceLogger traceLogger;

    @Autowired
    public PaymentService(ExternalServiceClient externalServiceClient, TraceLogger traceLogger) {
        this.externalServiceClient = externalServiceClient;
        this.traceLogger = traceLogger;
    }

    public ApiResponse processPayment(String url, PaymentRequest request) {
        Instant overallStart = Instant.now();
        traceLogger.logStep("SERVICE_START", "Processing payment flow");
        
        String targetRegion = traceLogger.detectRegion(url);
        Instant outboundStart = Instant.now();
        Object result;
        Instant outboundEnd;
        
        try {
            result = externalServiceClient.postPayment(url, request);
        } catch (Exception e) {
            result = Map.of("error", e.getMessage(), "status", "EXTERNAL_CALL_FAILED");
        } finally {
            outboundEnd = Instant.now();
        }
        
        Instant overallEnd = Instant.now();
        String traceRoute = traceLogger.buildVisualRoute("Client", "PaymentService", url, "Client");
        
        return buildApiResponse(result, outboundStart, outboundEnd, overallStart, overallEnd, traceRoute, targetRegion);
    }

    public ApiResponse proxyRequest(ProxyRequest proxyRequest) {
        Instant overallStart = Instant.now();
        traceLogger.logStep("SERVICE_START", "Processing proxy flow");
        
        String targetRegion = traceLogger.detectRegion(proxyRequest.getUrl());
        Instant outboundStart = Instant.now();
        Object result;
        Instant outboundEnd;
        
        try {
            result = externalServiceClient.executeDynamicRequest(
                    proxyRequest.getMethod(),
                    proxyRequest.getUrl(), 
                    proxyRequest.getHeaders(), 
                    proxyRequest.getParams(), 
                    proxyRequest.getData()
            );
        } catch (Exception e) {
            result = Map.of("error", e.getMessage(), "status", "PROXY_CALL_FAILED");
        } finally {
            outboundEnd = Instant.now();
        }
        
        Instant overallEnd = Instant.now();
        String traceRoute = traceLogger.buildVisualRoute("Client", "PaymentService", proxyRequest.getUrl(), "Client");
        
        return buildApiResponse(result, outboundStart, outboundEnd, overallStart, overallEnd, traceRoute, targetRegion);
    }

    private ApiResponse buildApiResponse(Object data, Instant outStart, Instant outEnd, Instant allStart, Instant allEnd, String traceRoute, String region) {
        long outboundDuration = Duration.between(outStart, outEnd).toMillis();
        long overallDuration = Duration.between(allStart, allEnd).toMillis();
        
        traceLogger.logStep("SERVICE_COMPLETE", String.format("Overall Journey took %dms", overallDuration));

        return ApiResponse.builder()
                .data(data)
                .metrics(ApiResponse.Metrics.builder()
                        .outboundRequestTime(outboundDuration + " ms")
                        .overallExecutionTime(overallDuration + " ms")
                        .traceRoute(traceRoute)
                        .region(region)
                        .timestamp(Instant.now().toString())
                        .build())
                .build();
    }
}
