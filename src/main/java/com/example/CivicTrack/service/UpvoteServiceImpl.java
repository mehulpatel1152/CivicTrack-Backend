package com.example.CivicTrack.service;

import com.example.CivicTrack.Repository.ComplaintRepository;
import com.example.CivicTrack.Repository.UpvoteRepository;
import com.example.CivicTrack.Repository.UserRepository;
import com.example.CivicTrack.model.Upvote;
import com.example.CivicTrack.model.User;
import com.example.CivicTrack.model.Complaint;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpvoteServiceImpl implements UpvoteService {

    private final UpvoteRepository upvoteRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    @Override
    public void upvoteByEmail(String email, UUID complaintId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent duplicate upvote
        if (upvoteRepository.existsByUserIdAndComplaintId(user.getId(), complaintId)) {
            return;
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        Upvote upvote = Upvote.builder()
                .user(user)
                .complaint(complaint)
                .build();

        upvoteRepository.save(upvote);
    }

    @Override
    public int getUpvoteCount(UUID complaintId) {
        return upvoteRepository.countByComplaintId(complaintId);
    }
}