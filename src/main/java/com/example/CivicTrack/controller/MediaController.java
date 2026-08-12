package com.example.CivicTrack.controller;

import com.example.CivicTrack.model.Media;
import com.example.CivicTrack.Repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaRepository mediaRepository;

    @GetMapping("/{complaintId}")
    public List<Media> getByComplaint(@PathVariable UUID complaintId) {
        return mediaRepository.findByComplaintId(complaintId);
    }
}