package com.example.CivicTrack.controller;

import com.example.CivicTrack.dto.UserResponseDTO;
import com.example.CivicTrack.model.User;
import com.example.CivicTrack.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .build());
    }
}