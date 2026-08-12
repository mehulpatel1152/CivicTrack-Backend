package com.example.CivicTrack.service;

import com.example.CivicTrack.Repository.ComplaintRepository;
import com.example.CivicTrack.Repository.UserRepository;
import com.example.CivicTrack.model.Role;
import com.example.CivicTrack.model.Status;

import com.example.CivicTrack.model.User;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();

        stats.put("total", complaintRepository.count());
        stats.put("pending", complaintRepository.countByStatus(Status.PENDING));
        stats.put("verified", complaintRepository.countByStatus(Status.VERIFIED));
        stats.put("assigned", complaintRepository.countByStatus(Status.ASSIGNED));
        stats.put("inProgress", complaintRepository.countByStatus(Status.IN_PROGRESS));
        stats.put("completed", complaintRepository.countByStatus(Status.COMPLETED));
        stats.put("rejected", complaintRepository.countByStatus(Status.REJECTED));

        return stats;
    }



    @Override
    public User createUser(User user) {

        if (user.getRole() == Role.USER) {
            throw new RuntimeException("Use signup for normal users");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }
}