package com.example.tmdt_bookingmakeup_app.dto.request.artist;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class TopCustomerDTO {
    private UUID customerId;
    private String customerName;
    private String email;
    private Long totalOrders;
    private Double totalSpent;
}
