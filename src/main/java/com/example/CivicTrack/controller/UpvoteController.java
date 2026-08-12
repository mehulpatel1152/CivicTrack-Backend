package com.example.CivicTrack.controller;

import com.example.CivicTrack.service.UpvoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/upvotes")
@RequiredArgsConstructor
public class UpvoteController {

    private final UpvoteService upvoteService;

    @PostMapping("/{complaintId}")
    public ResponseEntity<String> upvote(
            @PathVariable UUID complaintId,
            Authentication authentication
    ) {

        String email = (String) authentication.getPrincipal();

        upvoteService.upvoteByEmail(email, complaintId);

        return ResponseEntity.ok("Upvoted successfully");
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<Integer> count(@PathVariable UUID complaintId) {
        return ResponseEntity.ok(
                upvoteService.getUpvoteCount(complaintId)
        );
    }
}