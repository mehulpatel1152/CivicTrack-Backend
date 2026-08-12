package com.example.CivicTrack.service;

import com.example.CivicTrack.Repository.*;
import com.example.CivicTrack.dto.CommentDTO;
import com.example.CivicTrack.model.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    @Override
    public Comment addComment(CommentDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Complaint complaint = complaintRepository.findById(dto.getComplaintId())
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .user(user)
                .complaint(complaint)
                .build();

        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByComplaint(UUID complaintId) {
        return commentRepository.findByComplaintId(complaintId);
    }
}