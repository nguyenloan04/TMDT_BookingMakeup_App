package com.example.tmdt_bookingmakeup_app.dto.response.statistics;

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
public class ServiceOwnerVerificationDto {
    private UUID userId;
    private String displayName;
    private String email;
    private String phone;
    private String bio;
    private Integer experienceYears;
    private ShowcaseType showcaseType;
    private String identityFront;
    private String identityBack;
    private String verificationStatus;
}
