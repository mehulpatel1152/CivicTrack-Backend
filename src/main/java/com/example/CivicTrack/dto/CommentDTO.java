package com.example.CivicTrack.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentDTO {
    private String content;
    private java.util.UUID complaintId;
}