package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private Object data;
    private Metrics metrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private String outboundRequestTime; // Duration of the external call
        private String overallExecutionTime; // Total time taken by the service
        private String timestamp; // Current time
    }
}
