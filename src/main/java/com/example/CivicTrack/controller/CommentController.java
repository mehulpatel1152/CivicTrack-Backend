package com.example.CivicTrack.controller;

import com.example.CivicTrack.dto.CommentDTO;
import com.example.CivicTrack.model.Comment;
import com.example.CivicTrack.service.CommentService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> addComment(@Valid @RequestBody CommentDTO dto, Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(commentService.addComment(dto, email));
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable UUID complaintId) {
        return ResponseEntity.ok(
                commentService.getCommentsByComplaint(complaintId)
        );
    }
}