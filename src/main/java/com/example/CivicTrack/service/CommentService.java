package com.example.CivicTrack.service;

import com.example.CivicTrack.dto.CommentDTO;
import com.example.CivicTrack.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {

//    Comment addComment(Comment comment);

    Comment addComment(CommentDTO dto, String email);

    List<Comment> getCommentsByComplaint(UUID complaintId);
}