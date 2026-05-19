package com.example.tmdt_bookingmakeup_app.models.user;
import com.example.tmdt_bookingmakeup_app.common.enums.ShowcaseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "service_owners")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOwner {
    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="bio",columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "showcase_type")
    private ShowcaseType showcaseType;

    @Column(name="identify_front")
    private String identityFront;

    @Column(name="identify_back")
    private String identityBack;

    @Column(name = "verification_status")
    private String verificationStatus = "pending";

//    @OneToMany(mappedBy = "provider")
//    private List<Service> services;
}
