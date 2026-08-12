package com.example.CivicTrack.controller;

import com.example.CivicTrack.dto.CreateUserDTO;
import com.example.CivicTrack.dto.UserResponseDTO;
import com.example.CivicTrack.model.*;
import com.example.CivicTrack.Repository.*;

import com.example.CivicTrack.service.EscalationService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EscalationService escalationService;

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();

        if (dto.getRole() == Role.DEPARTMENT) {

            if (dto.getDepartmentId() == null) {
                return ResponseEntity.badRequest()
                        .body("Department ID is required for DEPARTMENT user");
            }

            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));

            user.setDepartment(dept);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> dtos = userRepository.findAll().stream()
                .map(u -> UserResponseDTO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .departmentName(u.getDepartment() != null ? u.getDepartment().getName() : null)
                        .build())
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/run-escalation")
    public ResponseEntity<String> runEscalation() {
        int count = escalationService.runEscalationCheck();
        return ResponseEntity.ok(count + " complaint(s) escalated");
    }

}