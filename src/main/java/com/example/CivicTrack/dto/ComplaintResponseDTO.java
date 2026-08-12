package com.example.CivicTrack.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ComplaintResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private String status;
    private Double latitude;
    private Double longitude;
    private String userEmail;
    private String departmentName;
    private java.time.LocalDateTime createdAt;
    private int upvoteCount;
    private String priority;
    private java.time.LocalDateTime dueDate;
    private boolean escalated;
}