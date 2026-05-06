package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String service;
    private String amount;
    private String invoiceId;
    private String merchantCallback;
    private String motoApiKey;
    private String subMerchantMid;
}
