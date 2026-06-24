package com.example.tmdt_bookingmakeup_app.services.payment;

import com.example.tmdt_bookingmakeup_app.config.VNPayConfig;
import com.example.tmdt_bookingmakeup_app.models.booking.Booking;
import com.example.tmdt_bookingmakeup_app.repositories.BookingRepository;
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
    private final VNPayConfig config;
    private final BookingRepository bookingRepository;
    private final String SERVER_IP = "127.0.0.1"; //TODO: Update to deployed server ip

    @Autowired
    public PaymentService(VNPayConfig config, BookingRepository bookingRepository) {
        this.config = config;
        this.bookingRepository = bookingRepository;
    }

    public String generatePayment(UUID bookingId, UUID customerId, String bankCode, String locale) {
        JsonObject json = new JsonObject();
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

            // BẢO MẬT: Kiểm tra xem người đang đăng nhập có phải chủ đơn không
            if (!booking.getCustomer().getId().equals(customerId)) {
                json.addProperty("code", "403");
                json.addProperty("message", "Bạn không có quyền thanh toán cho đơn này!");
                return json.toString();
            }

            long amount = (long) (booking.getDepositAmount() * 100);

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", config.vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount));
            vnpParams.put("vnp_CurrCode", "VND");

            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParams.put("vnp_BankCode", bankCode);
            }

            vnpParams.put("vnp_TxnRef", booking.getId().toString());
            vnpParams.put("vnp_OrderInfo", "Thanh toan tien coc dat lich " + booking.getId().toString().substring(0,8));
            vnpParams.put("vnp_OrderType", "250000");
            vnpParams.put("vnp_Locale", (locale != null && !locale.isEmpty()) ? locale : "vn");
            vnpParams.put("vnp_ReturnUrl", config.vnpReturnUrl);
            vnpParams.put("vnp_IpAddr", SERVER_IP);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnpParams.put("vnp_CreateDate", formatter.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 15);
            vnpParams.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (!fieldValue.isEmpty())) {
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
            return json.toString();
        } catch (Exception e) {
            log.error("Payment Generation Error: {}", e.getMessage());
            json.addProperty("code", "99");
            json.addProperty("message", "Lỗi hệ thống: " + e.getMessage());
            return json.toString();
        }
    }
}