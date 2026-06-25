package com.example.tmdt_bookingmakeup_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
public class SePayConfig {
    @Value("${sepay.merchant-id}")
    public String merchantId;

    @Value("${sepay.secret-key}")
    public String secretKey;

    @Value("${sepay.pay-url}")
    public String payUrl;

    public static final List<String> SIGNED_FIELDS = List.of(
            "merchant", "operation", "payment_method", "order_amount", "currency",
            "order_invoice_number", "order_description", "customer_id",
            "success_url", "error_url", "cancel_url"
    );

    public String generateSignature(Map<String, String> fields) throws Exception {
        StringBuilder signedData = new StringBuilder();

        for (String field : SIGNED_FIELDS) {
            if (fields.containsKey(field) && fields.get(field) != null) {
                if (!signedData.isEmpty()) {
                    signedData.append(",");
                }
                signedData.append(field).append("=").append(fields.get(field));
            }
        }

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);

        byte[] hash = hmacSha256.doFinal(signedData.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}