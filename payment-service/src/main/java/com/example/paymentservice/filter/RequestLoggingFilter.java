package com.example.paymentservice.filter;

import com.example.paymentservice.util.TraceLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_KEY = "requestId";
    private final TraceLogger traceLogger;

    @Autowired
    public RequestLoggingFilter(TraceLogger traceLogger) {
        this.traceLogger = traceLogger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_KEY, requestId);
        long startTime = System.currentTimeMillis();

        traceLogger.logStep("INCOMING", String.format("%s %s | Client IP: %s", 
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr()));

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            
            traceLogger.logStep("REQUEST BODY", requestBody.isEmpty() ? "[EMPTY]" : requestBody);
            traceLogger.logStep("RESPONSE BODY", responseBody.isEmpty() ? "[EMPTY]" : responseBody);
            
            traceLogger.logLatency("TOTAL_API_JOURNEY", duration);
            traceLogger.logStep("OUTGOING", String.format("Status: %d | Total Journey Duration: %dms", 
                    response.getStatus(), duration));
            
            responseWrapper.copyBodyToResponse();
            MDC.remove(REQUEST_ID_KEY);
        }
    }
}
