package com.example.tmdt_bookingmakeup_app.dto.response.user;

import com.example.tmdt_bookingmakeup_app.common.enums.ShowcaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOwnerProfileDto {
    private UUID userId;
    private String bio;
    private Integer experienceYears;
    private ShowcaseType showcaseType;
    private String identityFront;
    private String identityBack;
    private String verificationStatus;
}
