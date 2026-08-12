package com.example.CivicTrack.service;

import com.example.CivicTrack.model.User;

public interface UserService {

    User register(User user);

    User getUserByEmail(String email);
}