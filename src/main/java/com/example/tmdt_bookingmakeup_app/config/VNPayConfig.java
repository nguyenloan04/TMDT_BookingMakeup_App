package com.example.tmdt_bookingmakeup_app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class VNPayConfig {
    @Value("${vnpay.tmn_code}")
    public static String vnpTmnCode;
    @Value("${vnpay.secret}")
    public static String vnpHashSecret;
    public static final String vnpPayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String transactionUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    /// Tạm
    public static final String vnpReturnUrl = "http://localhost:8080/purchase";

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    //Tạo HMAC SHA512
    public static String generateHmacSHA512(String key, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(2 * hashBytes.length);
            for (byte b : hashBytes) {
                //Chuyển từ byte thành hệ 16 (hex)
                //b & 0xff là chuyển byte từ -128 - 127 thành 0 - 255
                hash.append(String.format("%02x", b & 0xff));
            }
            return hash.toString();
        }
        catch (Exception e) {
            throw new RuntimeException("Tạo HMAC SHA512 thất bại", e);
        }
    }
}
