package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.common.enums.OrderStatus;
import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.services.OrderService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentIPNService {
    private final OrderService orderService;

    @Autowired
    public PaymentIPNService(OrderService orderService) {
        this.orderService = orderService;
    }

    public String generatePaymentIpn(Map<String, String> allParams) {
        JsonObject json = new JsonObject();
        String rspCode;
        String message;
        try {
            Map<String, String> fields = new HashMap<>(allParams);

            String vnpSecureHash = allParams.get("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            //Checksum
            String signValue = VNPayConfig.generateHmacSHA512(fields.keySet().toString(), fields.values().toString());

            if (signValue.equals(vnpSecureHash)) {

                String requestOrderStatus = allParams.get("orderStatus");
                String requestOrderId = allParams.get("orderId");
                String requestsAmount = allParams.get("amount");
                UUID orderId = UUID.fromString(requestOrderId);
                int amount = requestsAmount != null ? Integer.parseInt(requestsAmount) : -1;
                OrderStatus orderStatus = requestOrderStatus != null && OrderStatus.PENDING.getValue() == Integer.parseInt(requestOrderStatus) ? OrderStatus.PENDING : null;

                boolean checkOrderId = orderService.getOrderById(orderId) != null; // vnp_TxnRef exists in your database
                boolean checkAmount = amount != -1; // vnp_Amount is valid (Check vnp_Amount VNPAY returns compared to the amount of the code (vnp_TxnRef) in Your database).
                //FIXME: Fix business logic here
                boolean checkOrderStatus = orderStatus == OrderStatus.CONFIRMED; // PaymnentStatus = 0 (pending)

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(allParams.get("vnp_ResponseCode"))) {
                                orderService.updateStatus(orderId, OrderStatus.PAID);
                            } else {
                                orderService.updateStatus(orderId, OrderStatus.PENDING);
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
