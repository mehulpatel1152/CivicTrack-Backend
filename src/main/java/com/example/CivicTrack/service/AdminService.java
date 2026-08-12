package com.example.CivicTrack.service;

import com.example.CivicTrack.model.User;

import java.util.Map;

public interface AdminService {

    Map<String, Object> getDashboardStats();

    User createUser(User user);
}