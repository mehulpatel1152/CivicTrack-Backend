package com.example.CivicTrack.Repository;

import com.example.CivicTrack.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    Page<Complaint> findByStatus(Status status, Pageable pageable);

    Page<Complaint> findByCategory(Category category, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.road) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.pincode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Complaint> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM complaint c WHERE " +
            "similarity(LOWER(c.title), LOWER(:title)) > 0.25 " +
            "AND ST_DWithin(c.location::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, 200)",
            nativeQuery = true)
    List<Complaint> findSimilarComplaints(
            @Param("title") String title,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );

    @Query(value = "SELECT * FROM complaint c WHERE ST_DWithin(" +
            "c.location::geography, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radius * 1000)",
            nativeQuery = true)
    List<Complaint> findNearby(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radiusKm
    );

    long countByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime time);

    long countByStatus(Status status);

    List<Complaint> findByDepartmentId(UUID departmentId);

    List<Complaint> findByUserId(UUID userId);

    List<Complaint> findByDueDateBeforeAndEscalatedFalseAndStatusNotIn(
            LocalDateTime time, List<Status> statuses);

}