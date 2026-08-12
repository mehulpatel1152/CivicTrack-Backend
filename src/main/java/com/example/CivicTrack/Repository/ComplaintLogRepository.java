package com.example.CivicTrack.Repository;

import com.example.CivicTrack.model.ComplaintLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComplaintLogRepository extends JpaRepository<ComplaintLog, UUID> {

    List<ComplaintLog> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
}