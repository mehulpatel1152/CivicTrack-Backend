package com.example.CivicTrack.service;

import java.util.UUID;

public interface UpvoteService {


    int getUpvoteCount(UUID complaintId);


    void upvoteByEmail(String email, UUID complaintId);
}