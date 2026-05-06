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
public class ProxyRequest {
    private String method;
    private String url;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Object data; // This is the actual body to be sent to the external API
}
