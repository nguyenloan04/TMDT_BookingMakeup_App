package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class PaymentIPNService {
    public String generatePaymentIpn(Map<String, String> allParams) {
        JsonObject json = new JsonObject();
        String rspCode = "";
        String message = "";
        try {
            Map<String, String> fields = new HashMap<>(allParams);

            String vnpSecureHash = allParams.get("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            //Checksum
            String signValue = VNPayConfig.generateHmacSHA512(fields.keySet().toString(), fields.values().toString());

            if (signValue.equals(vnpSecureHash)) {
//                OrderService orderService = new OrderService(request);
                String requestOrderStatus = allParams.get("orderStatus");
                String requestOrderId = allParams.get("orderId");
                String requestsAmount = allParams.get("amount");
                int orderId = requestOrderId != null ? Integer.parseInt(requestOrderId) : -1;
                int amount = requestsAmount != null ? Integer.parseInt(requestsAmount) : -1;
//                OrderStatus orderStatus = requestOrderStatus != null && OrderStatus.PENDING_CONFIRMATION.getCode() == Integer.parseInt(requestOrderStatus) ? OrderStatus.PENDING_CONFIRMATION : null;

                //FIXME: Add OrderService
//                boolean checkOrderId = orderId != -1 && orderService.getOrderById(orderId) != null; // vnp_TxnRef exists in your database
//                boolean checkAmount = amount != -1; // vnp_Amount is valid (Check vnp_Amount VNPAY returns compared to the amount of the code (vnp_TxnRef) in the Your database).
//                boolean checkOrderStatus = orderStatus == OrderStatus.PENDING_CONFIRMATION; // PaymnentStatus = 0 (pending)
                boolean checkOrderId = false;
                boolean checkAmount = false;
                boolean checkOrderStatus = false;

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(allParams.get("vnp_ResponseCode"))) {
                                //Cập nhật PaymentStatus thành
                                // PaymentStatus.PAYMENT_PAID
//                                orderService.updateStatus(orderId, OrderStatus.WAITING_FOR_PICKUP.getCode());
                            } else {
                                //Cập nhật PaymentStatus thành
                                //PaymentStatus.PAYMENT_UNPAID
//                                orderService.updateStatus(orderId, OrderStatus.WAITING_FOR_PICKUP.getCode());
                            }
                            rspCode = "00";
                            message = "Confirm Success";
                        } else {
                            rspCode = "02";
                            message = "Order already confirmed";
                        }
                    } else {
                        rspCode = "04";
                        message = "Invalid Amount";
                    }
                } else {
                    rspCode = "01";
                    message = "Order not Found";
                }
            } else {
                rspCode = "97";
                message = "Invalid Checksum";
            }
            json.addProperty("rspCode", rspCode);
            json.addProperty("message", message);
            return json.toString();
        }
        catch (Exception e) {
            rspCode = "99";
            message = "Unknown error";
            json.addProperty("rspCode", rspCode);
            json.addProperty("message", message);
            return json.toString();
        }
    }
}
