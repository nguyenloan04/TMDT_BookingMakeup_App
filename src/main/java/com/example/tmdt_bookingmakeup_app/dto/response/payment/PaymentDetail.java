package com.example.tmdt_bookingmakeup_app.dto.response.payment;

import java.util.List;
import java.util.UUID;

public record PaymentDetail(
        UUID userInfo,
        //TODO: Change this datatype to Address
        List<String> fullUserAddress,
        //TODO: Change this datatype to Address
        String address,
        int addressId

) {
}
