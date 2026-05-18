package com.example.tmdt_bookingmakeup_app.common.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum OrderStatus {
    PENDING(0), CANCELED(1), CONFIRMED(2), REFUND(3), PAID(4), IN_PROGRESS(5), COMPLETED(6);
    private int value;
    OrderStatus(int value) {}
}
