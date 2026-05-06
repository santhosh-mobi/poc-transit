package com.example.paymentservice.client;

import com.example.paymentservice.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class ExternalServiceClient {

    private final RestTemplate restTemplate;
    
    @Autowired
    public ExternalServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object postPayment(String externalServiceUrl, PaymentRequest request) {
        log.debug("Calling external payment API: {}", externalServiceUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            return restTemplate.postForObject(externalServiceUrl, entity, Object.class);
        } catch (Exception e) {
            log.error("External API call failed: {}", e.getMessage());
            throw e;
        }
    }

    public Object executeDynamicRequest(String method, String url, Map<String, String> headersMap, Map<String, String> params, Object body) {
        log.info("Executing dynamic {} request to URL: {}", method, url);
        
        // 1. Build URL with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null) {
            params.forEach(builder::queryParam);
        }
        String finalUrl = builder.toUriString();

        // 2. Build Headers
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::add);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        // 3. Build Entity
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        // 4. Resolve HTTP Method
        HttpMethod httpMethod = HttpMethod.valueOf(method != null ? method.toUpperCase() : "POST");

        // 5. Execute request
        try {
            log.debug("Sending {} to: {}", httpMethod, finalUrl);
            return restTemplate.exchange(finalUrl, httpMethod, entity, Object.class).getBody();
        } catch (Exception e) {
            log.error("Dynamic API call failed: {}", e.getMessage());
            throw e;
        }
    }
}
