package com.example.paymentservice.controller;

import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.ProxyRequest;
import com.example.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API", description = "Endpoints for processing payments via external gateway")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Initiate Payment", description = "Calls an external service to process a payment with a fixed schema")
    @PostMapping("/initiate")
    public ApiResponse initiatePayment(@RequestParam String url, @RequestBody PaymentRequest request) {
        log.info("REST request to initiate payment: {} for URL: {}", request.getInvoiceId(), url);
        return paymentService.processPayment(url, request);
    }

    @Operation(summary = "Proxy Request", description = "Generic proxy to call any external API with dynamic headers, params, and data provided in the body")
    @PostMapping("/proxy")
    public ApiResponse proxyRequest(@RequestBody ProxyRequest proxyRequest) {
        log.info("REST request to proxy to URL: {}", proxyRequest.getUrl());
        return paymentService.proxyRequest(proxyRequest);
    }


}
