package com.example.CivicTrack.Repository;

import com.example.CivicTrack.model.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, UUID> {

    boolean existsByUserIdAndComplaintId(UUID userId, UUID complaintId);

    int countByComplaintId(UUID complaintId);
}