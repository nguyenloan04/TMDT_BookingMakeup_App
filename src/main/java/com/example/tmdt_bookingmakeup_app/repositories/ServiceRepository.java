package com.example.tmdt_bookingmakeup_app.repositories;

import com.example.tmdt_bookingmakeup_app.models.services.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    /**
     * Tìm kiếm service với các bộ lọc: keyword, location, category, price range, rating.
     * Dùng native query để join với artists và users lấy thêm thông tin.
     */
    @Query(value = """
            SELECT s.*
            FROM "e-commerce".services s
            JOIN "e-commerce".service_owners so ON so.user_id = s.owner_id
            JOIN "e-commerce".users u ON u.id = so.user_id
            LEFT JOIN "e-commerce".artists a ON a.owner_id = so.user_id
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR :location = ''
                   OR LOWER(CAST(u.address AS TEXT)) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:category IS NULL OR :category = ''
                   OR LOWER(s.category) = LOWER(:category))
              AND (:minPrice IS NULL OR s.price >= CAST(:minPrice AS DOUBLE PRECISION))
              AND (:maxPrice IS NULL OR s.price <= CAST(:maxPrice AS DOUBLE PRECISION))
              AND (:minRating IS NULL OR COALESCE(a.average_rating, 0) >= CAST(:minRating AS DOUBLE PRECISION))
            """,
            countQuery = """
            SELECT COUNT(s.id)
            FROM "e-commerce".services s
            JOIN "e-commerce".service_owners so ON so.user_id = s.owner_id
            JOIN "e-commerce".users u ON u.id = so.user_id
            LEFT JOIN "e-commerce".artists a ON a.owner_id = so.user_id
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR :location = ''
                   OR LOWER(CAST(u.address AS TEXT)) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:category IS NULL OR :category = ''
                   OR LOWER(s.category) = LOWER(:category))
              AND (:minPrice IS NULL OR s.price >= CAST(:minPrice AS DOUBLE PRECISION))
              AND (:maxPrice IS NULL OR s.price <= CAST(:maxPrice AS DOUBLE PRECISION))
              AND (:minRating IS NULL OR COALESCE(a.average_rating, 0) >= CAST(:minRating AS DOUBLE PRECISION))
            """,
            nativeQuery = true)
    Page<Service> searchServices(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    @Query("SELECT MIN(s.price) FROM Service s WHERE s.owner.userId = :ownerId AND s.isActive = true")
    Double findMinPriceByOwnerId(@Param("ownerId") UUID ownerId);
}
