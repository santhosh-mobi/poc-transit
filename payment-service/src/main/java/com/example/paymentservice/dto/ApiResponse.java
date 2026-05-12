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
    private Diagnostics diagnostics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Diagnostics {
        private String latency;
        private boolean isReachable;
        private java.util.List<String> route;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        private String outboundRequestTime; // Duration of the external call
        private String overallExecutionTime; // Total time taken by the service
        private String traceRoute; // Path of the request
        private String region; // Region where external call was made
        private String timestamp; // Current time
    }
}
