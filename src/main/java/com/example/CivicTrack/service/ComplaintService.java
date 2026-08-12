package com.example.CivicTrack.service;

import com.example.CivicTrack.dto.ComplaintRequestDTO;
import com.example.CivicTrack.dto.ComplaintResponseDTO;
import com.example.CivicTrack.dto.DashboardResponseDTO;
import com.example.CivicTrack.model.Complaint;
import com.example.CivicTrack.model.ComplaintLog;

import com.example.CivicTrack.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.example.CivicTrack.model.Status;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {

    // 🔥 NEW CORRECT METHOD
    Complaint createComplaint(
            ComplaintRequestDTO dto,
            List<MultipartFile> files,
            String email
    );

    Complaint verifyComplaint(UUID complaintId);

    Complaint rejectComplaint(UUID complaintId, String reason);

    Complaint assignComplaint(UUID complaintId, UUID departmentId);

    Complaint startWork(UUID complaintId);

//    Complaint completeComplaint(UUID complaintId, List<MultipartFile> files);

    Page<Complaint> getAllComplaints(Pageable pageable);

    Page<Complaint> searchComplaints(String keyword, Pageable pageable);

    List<Complaint> getNearbyComplaints(Double lat, Double lng, Double radius);

    List<ComplaintLog> getComplaintHistory(UUID complaintId);

    ComplaintResponseDTO mapToDTO(Complaint complaint);

//    Complaint markDone(UUID complaintId);
    Complaint markDone(UUID complaintId, List<MultipartFile> files);

    Complaint approveCompletion(UUID complaintId);

    Complaint rejectAfterDone(UUID complaintId, String reason);

    List<Complaint> getComplaintsForDepartment(String email);

    DashboardResponseDTO getDashboardStats();

    Complaint startWork(UUID complaintId, String email);
    Complaint markDone(UUID complaintId, List<MultipartFile> files, String email);

    Complaint getComplaintById(UUID complaintId);

    List<Complaint> getComplaintsForUser(String email);

    Page<Complaint> getComplaintsByStatus(Status status, Pageable pageable);

    List<Complaint> getSimilarComplaints(UUID complaintId);

    List<Complaint> checkPotentialDuplicates(String title, Double lat, Double lng);

}