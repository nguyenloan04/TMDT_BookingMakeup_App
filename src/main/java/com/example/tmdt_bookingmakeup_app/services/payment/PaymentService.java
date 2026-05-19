package com.example.tmdt_bookingmakeup_app.services.payment;


import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.services.OrderService;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class PaymentService {
    VNPayConfig config;
    OrderService orderService;

    @Autowired
    public PaymentService(VNPayConfig config, OrderService orderService) {
        this.config = config;
        this.orderService = orderService;
    }

    public String generatePayment(Map<String, String> params) {
        JsonObject json = new JsonObject();
        try {
             // Get all selected product to generate payment

//            User account = (User) session.getAttribute("account");
//            Cart cart = account.getCart();
//            Map<String, CartProduct> listSelectedProductCode = new HashMap<>();
//            if (session.getAttribute("selectedProducts") != null) {
//                Map<?, ?> tempMap = (Map<?, ?>) session.getAttribute("selectedProducts");
//                if (tempMap.keySet().stream().allMatch(k -> k instanceof String) &&
//                        tempMap.values().stream().allMatch(v -> v instanceof CartProduct)) {
//                    listSelectedProductCode = (Map<String, CartProduct>) tempMap;
//
//                }
//            }

            String address = params.get("address");
            if (address == null) return json.toString();
//
//            int deliveryQuantity = listSelectedProductCode.values().stream().mapToInt(CartProduct::getQuantity).sum();
//            int deliveryPrice = OrderFeeService.generateOrderFeeData(address.getProvince(), address.getDistrict(), address.getWard(), deliveryQuantity);
//            List<String> listCartCode = listSelectedProductCode.keySet().stream().toList();
//
//            double totalPrice = cart.getTotalPrice(listCartCode);
//            double finalPrice = cart.getFinalPrice(listCartCode, deliveryPrice);

//            int amount = (int) finalPrice * 100;
            int orderId = Integer.parseInt(params.get("orderId"));
            String bankCode = params.get("bankcode");
            //Map chứa các param
            Map<String, String> vnpParams = new HashMap<>();
            //Config các param cần thiết
            String vnpVersion = "2.1.0";
            String vnpCommand = "pay";
            String vnpTmnCode = config.vnpTmnCode;
            String vnpTxnRef = String.valueOf(orderId);
//            String vnpOrderInfo = "Thanh toan don hang id " + orderId + ". So tien " + amount + " VND";
            String vnpOrderType = "230001"; //TODO: Change order type in vnpay doc
            String vnpIpAddr = "127.0.0.1"; //FIXME: Fix this
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(calendar.getTime());
            calendar.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(calendar.getTime());

            vnpParams.put("vnp_Version", vnpVersion);
            vnpParams.put("vnp_Command", vnpCommand);
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
//            vnpParams.put("vnp_Amount", String.valueOf(amount));  //FIXME: Fix this field
            vnpParams.put("vnp_CurrCode", "VND");
            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParams.put("vnp_BankCode", bankCode);
            }
            vnpParams.put("vnp_TxnRef", vnpTxnRef);
//            vnpParams.put("vnp_OrderInfo", vnpOrderInfo); //FIXME: Fix this field
            vnpParams.put("vnp_OrderType", vnpOrderType);
            String locale = params.get("language");
            vnpParams.put("vnp_Locale", (locale != null && !locale.isEmpty()) ? locale : "vn");
//            vnp_Params.put("vnp_IpnUrl", "https://example.com/vnpay_ipn");    //Sau khi deploy
            vnpParams.put("vnp_ReturnUrl", config.vnpReturnUrl);
            vnpParams.put("vnp_IpAddr", vnpIpAddr);
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            //Config for sandbox
            // Biling detail

//        vnpParams.put("vnp_Bill_Mobile", params.get("txt_billing_mobile"));   //Số điện thoại user
//        vnpParams.put("vnp_Bill_Email", params.get("txt_billing_email")); //Email user
//        String fullName = (params.get("txt_billing_fullname")).trim();    //Username của user
//        //Lấy firstname và lastname cho billing
//        if (!fullName.isEmpty()) {
//            int idx = fullName.indexOf(' ');
//            String firstName = fullName.substring(0, idx);
//            String lastName = fullName.substring(fullName.lastIndexOf(' ') + 1);
//            vnpParams.put("vnp_Bill_FirstName", firstName);
//            vnpParams.put("vnp_Bill_LastName", lastName);
//        }
//        vnpParams.put("vnp_Bill_Address", params.get("txt_inv_addr1"));   //Địa chỉ của user (full Str)
//        vnpParams.put("vnp_Bill_City", params.get("txt_bill_city"));  //Thành phố trong address
//        vnpParams.put("vnp_Bill_Country", params.get("txt_bill_country"));    //Quốc gia
//        if (params.get("txt_bill_state") != null && !params.get("txt_bill_state").isEmpty()) {
//            vnpParams.put("vnp_Bill_State", params.get("txt_bill_state"));    //Quận/huyện
//        }

             // Invoice Config
//        vnpParams.put("vnp_Inv_Phone", params.get("txt_inv_mobile"));
//        vnpParams.put("vnp_Inv_Email", params.get("txt_inv_email"));
//        vnpParams.put("vnp_Inv_Customer", params.get("txt_inv_customer"));
//        vnpParams.put("vnp_Inv_Address", params.get("txt_inv_addr1"));
//        vnpParams.put("vnp_Inv_Company", params.get("txt_inv_company"));
//        vnpParams.put("vnp_Inv_Taxcode", params.get("txt_inv_taxcode"));
//        vnpParams.put("vnp_Inv_Type", params.get("cbo_inv_type"));

            List<String> fieldNames = new ArrayList(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnpSecureHash = VNPayConfig.generateHmacSHA512(config.vnpHashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = config.vnpPayUrl + "?" + queryUrl;
            json.addProperty("code", "00");
            json.addProperty("message", "success");
            json.addProperty("data", paymentUrl);
            log.info(json.toString());
            return json.toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            return json.toString();
        }
    }
}
