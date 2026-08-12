package com.example.CivicTrack.dto;

import com.example.CivicTrack.model.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Category category;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

//    @NotNull
//    private UUID userId;

    private String area;
    private String road;
    private String pincode;
}