package com.example.CivicTrack.dto;

import com.example.CivicTrack.model.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDTO {

    private String name;
    private String email;
    private String password;
    private Role role;


    private UUID departmentId;
}