package com.example.CivicTrack.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private String departmentName;
}