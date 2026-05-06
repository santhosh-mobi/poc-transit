package com.example.paymentservice.service;

import com.example.paymentservice.client.ExternalServiceClient;
import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.ProxyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    private final ExternalServiceClient externalServiceClient;

    @Autowired
    public PaymentService(ExternalServiceClient externalServiceClient) {
        this.externalServiceClient = externalServiceClient;
    }

    public ApiResponse processPayment(String url, PaymentRequest request) {
        Instant overallStart = Instant.now();
        log.info("Processing payment for invoice ID: {}", request.getInvoiceId());
        
        Instant outboundStart = Instant.now();
        Object result;
        Instant outboundEnd;
        try {
            result = externalServiceClient.postPayment(url, request);
        } catch (Exception e) {
            log.error("Error calling external payment service: {}", e.getMessage());
            result = Map.of("error", e.getMessage(), "status", "EXTERNAL_CALL_FAILED");
        } finally {
            outboundEnd = Instant.now();
        }
        
        Instant overallEnd = Instant.now();
        return buildApiResponse(result, outboundStart, outboundEnd, overallStart, overallEnd);
    }

    public ApiResponse proxyRequest(ProxyRequest proxyRequest) {
        Instant overallStart = Instant.now();
        log.info("Proxying request to: {}", proxyRequest.getUrl());
        
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
            log.info("inbound response :: {} " , result);
        } catch (Exception e) {
            log.error("Error in proxy request: {}", e.getMessage());
            result = Map.of("error", e.getMessage(), "status", "PROXY_CALL_FAILED");
        } finally {
            outboundEnd = Instant.now();
        }
        
        Instant overallEnd = Instant.now();
        return buildApiResponse(result, outboundStart, outboundEnd, overallStart, overallEnd);
    }

    private ApiResponse buildApiResponse(Object data, Instant outStart, Instant outEnd, Instant allStart, Instant allEnd) {
        long outboundDuration = Duration.between(outStart, outEnd).toMillis();
        long overallDuration = Duration.between(allStart, allEnd).toMillis();
        
        log.info("Execution Metrics - Outbound Request Time: {} ms, Overall Execution Time: {} ms", outboundDuration, overallDuration);

        return ApiResponse.builder()
                .data(data)
                .metrics(ApiResponse.Metrics.builder()
                        .outboundRequestTime(outboundDuration + " ms")
                        .overallExecutionTime(overallDuration + " ms")
                        .timestamp(Instant.now().toString())
                        .build())
                .build();
    }
}
