package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.common.enums.UserRole;
import com.example.tmdt_bookingmakeup_app.models.user.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    Page<Artist> findByOwner_User_RoleNot(UserRole role, Pageable pageable);

    List<Artist> findByOwnerUserId(UUID ownerId);

    List<Artist> findBySpecializationContainingIgnoreCase(String specialization);

    @Modifying
    @Query("UPDATE Artist a SET a.followCount = a.followCount + 1 WHERE a.id = :artistId")
    void incrementFollowCount(@Param("artistId") UUID artistId);

    @Modifying
    @Query("UPDATE Artist a SET a.followCount = a.followCount - 1 WHERE a.id = :artistId AND a.followCount > 0")
    void decrementFollowCount(@Param("artistId") UUID artistId);
}
