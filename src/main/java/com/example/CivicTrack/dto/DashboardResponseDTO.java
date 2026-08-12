package com.example.CivicTrack.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDTO {

    private long total;
    private long pending;
    private long verified;
    private long assigned;
    private long inProgress;
    private long done;
    private long completed;
}