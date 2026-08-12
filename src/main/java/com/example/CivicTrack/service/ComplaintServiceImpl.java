package com.example.CivicTrack.service;

import com.example.CivicTrack.dto.ComplaintRequestDTO;
import com.example.CivicTrack.dto.ComplaintResponseDTO;
import com.example.CivicTrack.dto.DashboardResponseDTO;
import com.example.CivicTrack.model.*;
import com.example.CivicTrack.Repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {



    @Override
    public Complaint startWork(UUID complaintId) {
        return null;
    }

    @Override
    public Complaint markDone(UUID complaintId, List<MultipartFile> files) {
        return null;
    }
    private final UpvoteRepository upvoteRepository;
    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintLogRepository logRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final MediaRepository mediaRepository;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    @Transactional
    public Complaint createComplaint(
            ComplaintRequestDTO dto,
            List<MultipartFile> files,
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = Complaint.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .area(dto.getArea())
                .road(dto.getRoad())
                .pincode(dto.getPincode())
                .user(user)
                .status(Status.PENDING)
                .build();

        complaint.setLatLng(dto.getLatitude(), dto.getLongitude());   // ← must come BEFORE the check below

        if (complaint.getLatitude() < 6 || complaint.getLatitude() > 37 ||
                complaint.getLongitude() < 68 || complaint.getLongitude() > 97) {
            throw new RuntimeException("Invalid India location");
        }

        long count = complaintRepository.countByUserIdAndCreatedAtAfter(
                user.getId(),
                LocalDateTime.now().toLocalDate().atStartOfDay()
        );

        if (count >= 5) {
            throw new RuntimeException("Daily complaint limit reached");
        }

        Complaint saved = complaintRepository.save(complaint);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {

                validateFile(file);

                String path = fileStorageService.saveFile(file);

                Media media = Media.builder()
                        .filePath(path)
                        .type(MediaType.BEFORE)
                        .complaint(saved)
                        .build();

                mediaRepository.save(media);
            }
        }

        log(saved, "CREATED");
        return saved;
    }

    private static final Map<Category, Integer> CATEGORY_SEVERITY = Map.of(
            Category.ROAD, 3,
            Category.WATER, 3,
            Category.ELECTRICITY, 3,
            Category.GARBAGE, 2,
            Category.OTHER, 1
    );

    private void assignPriorityAndDueDate(Complaint complaint) {
        int severity = CATEGORY_SEVERITY.getOrDefault(complaint.getCategory(), 1);
        long daysWaiting = java.time.temporal.ChronoUnit.DAYS.between(
                complaint.getCreatedAt(), LocalDateTime.now());

        int score = (severity * 5) + (complaint.getUpvotes() * 3) + ((int) daysWaiting * 2);

        Priority priority;
        int slaDays;
        if (score >= 25) {
            priority = Priority.HIGH;
            slaDays = 3;
        } else if (score >= 15) {
            priority = Priority.MEDIUM;
            slaDays = 5;
        } else {
            priority = Priority.LOW;
            slaDays = 7;
        }

        complaint.setPriority(priority);
        complaint.setDueDate(LocalDateTime.now().plusDays(slaDays));
    }

    @Override
    public Complaint verifyComplaint(UUID complaintId) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only PENDING complaints can be verified");
        }

        complaint.setStatus(Status.VERIFIED);
        assignPriorityAndDueDate(complaint);
        Complaint updated = complaintRepository.save(complaint);

        log(updated, "VERIFIED");
        return updated;
    }

    @Override
    public Complaint rejectComplaint(UUID complaintId, String reason) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only PENDING complaints can be rejected");
        }

        complaint.setStatus(Status.REJECTED);
        complaint.setRejectionReason(reason);

        Complaint updated = complaintRepository.save(complaint);

        log(updated, "REJECTED");
        return updated;
    }

    @Override
    public Complaint assignComplaint(UUID complaintId, UUID departmentId) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.VERIFIED) {
            throw new RuntimeException("Only VERIFIED complaints can be assigned");
        }

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        complaint.setDepartment(dept);
        complaint.setStatus(Status.ASSIGNED);

        Complaint updated = complaintRepository.save(complaint);

        log(updated, "ASSIGNED");
        return updated;
    }

    @Override
    public Complaint startWork(UUID complaintId, String email) {
        Complaint complaint = getComplaint(complaintId);
        verifyDepartmentOwnership(complaint, email);

        if (complaint.getStatus() != Status.ASSIGNED) {
            throw new RuntimeException("Only ASSIGNED complaints can start");
        }

        complaint.setStatus(Status.IN_PROGRESS);
        Complaint updated = complaintRepository.save(complaint);

        log(updated, "IN_PROGRESS");
        return updated;
    }

    @Transactional
    public Complaint completeComplaint(UUID complaintId, List<MultipartFile> files) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.IN_PROGRESS) {
            throw new RuntimeException("Only IN_PROGRESS complaints can be completed");
        }

        complaint.setStatus(Status.COMPLETED);
        Complaint updated = complaintRepository.save(complaint);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {

                validateFile(file);

                String path = fileStorageService.saveFile(file);

                Media media = Media.builder()
                        .filePath(path)
                        .type(MediaType.AFTER)
                        .complaint(updated)
                        .build();

                mediaRepository.save(media);
            }
        }

        log(updated, "COMPLETED");
        return updated;
    }

    @Override
    public Page<Complaint> getAllComplaints(Pageable pageable) {
        return complaintRepository.findAll(pageable);
    }

    @Override
    public Page<Complaint> searchComplaints(String keyword, Pageable pageable) {
        return complaintRepository.search(keyword, pageable);
    }

    @Override
    public List<Complaint> getNearbyComplaints(Double lat, Double lng, Double radius) {
        return complaintRepository.findNearby(lat, lng, radius);
    }

    @Override
    public List<ComplaintLog> getComplaintHistory(UUID complaintId) {
        return logRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId);
    }

    private Complaint getComplaint(UUID id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
    }

    private void log(Complaint complaint, String action) {
        ComplaintLog log = ComplaintLog.builder()
                .complaint(complaint)
                .action(action)
                .build();

        logRepository.save(log);
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File too large: " + file.getOriginalFilename());
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Unsupported file type: " + file.getOriginalFilename());
        }
    }

    @Override
    public ComplaintResponseDTO mapToDTO(Complaint complaint) {
        return ComplaintResponseDTO.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory() != null ? complaint.getCategory().name() : null)
                .status(complaint.getStatus().name())
                .latitude(complaint.getLatitude())
                .longitude(complaint.getLongitude())
                .userEmail(complaint.getUser().getEmail())
                .departmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : null)
                .createdAt(complaint.getCreatedAt())
                .upvoteCount(upvoteRepository.countByComplaintId(complaint.getId()))
                .priority(complaint.getPriority() != null ? complaint.getPriority().name() : null)
                .dueDate(complaint.getDueDate())
                .escalated(complaint.isEscalated())
                .build();
    }


    public Complaint getComplaintById(UUID id) {
        return getComplaint(id);
    }

    @Override
    @Transactional
    public Complaint markDone(UUID complaintId, List<MultipartFile> files, String email) {
        Complaint complaint = getComplaint(complaintId);
        verifyDepartmentOwnership(complaint, email);
        if (complaint.getStatus() != Status.IN_PROGRESS) {
            throw new RuntimeException("Complaint must be IN_PROGRESS");
        }

        complaint.setStatus(Status.DONE);
        Complaint updated = complaintRepository.save(complaint);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                validateFile(file);
                String path = fileStorageService.saveFile(file);

                Media media = Media.builder()
                        .filePath(path)
                        .type(MediaType.AFTER)
                        .complaint(updated)
                        .build();

                mediaRepository.save(media);
            }
        }

        log(updated, "MARKED_DONE_BY_DEPARTMENT");
        return updated;
    }
    @Override
    public Complaint approveCompletion(UUID complaintId) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.DONE) {
            throw new RuntimeException("Complaint must be DONE before completion");
        }

        complaint.setStatus(Status.COMPLETED);
        Complaint updated = complaintRepository.save(complaint);

        log(updated, "APPROVED_BY_AUTHORITY");
        return updated;
    }

    @Override
    public Complaint rejectAfterDone(UUID complaintId, String reason) {
        Complaint complaint = getComplaint(complaintId);

        if (complaint.getStatus() != Status.DONE) {
            throw new RuntimeException("Complaint must be DONE to reject");
        }

        complaint.setStatus(Status.IN_PROGRESS);
        complaint.setRejectionReason(reason);

        Complaint updated = complaintRepository.save(complaint);

        log(updated, "REJECTED_AFTER_DONE: " + reason);
        return updated;
    }

    @Override
    public List<Complaint> getComplaintsForDepartment(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDepartment() == null) {
            throw new RuntimeException("User is not assigned to any department");
        }

        return complaintRepository.findByDepartmentId(user.getDepartment().getId());
    }

    @Override
    public DashboardResponseDTO getDashboardStats() {
        return DashboardResponseDTO.builder()
                .total(complaintRepository.count())
                .pending(complaintRepository.countByStatus(Status.PENDING))
                .verified(complaintRepository.countByStatus(Status.VERIFIED))
                .assigned(complaintRepository.countByStatus(Status.ASSIGNED))
                .inProgress(complaintRepository.countByStatus(Status.IN_PROGRESS))
                .done(complaintRepository.countByStatus(Status.DONE))
                .completed(complaintRepository.countByStatus(Status.COMPLETED))
                .build();
    }

    private void verifyDepartmentOwnership(Complaint complaint, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDepartment() == null ||
                complaint.getDepartment() == null ||
                !user.getDepartment().getId().equals(complaint.getDepartment().getId())) {
            throw new RuntimeException("You are not authorized to act on this complaint");
        }
    }

    @Override
    public List<Complaint> getComplaintsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return complaintRepository.findByUserId(user.getId());
    }

    @Override
    public Page<Complaint> getComplaintsByStatus(Status status, Pageable pageable) {
        return complaintRepository.findByStatus(status, pageable);
    }

    @Override
    public List<Complaint> getSimilarComplaints(UUID complaintId) {
        Complaint complaint = getComplaint(complaintId);
        return complaintRepository.findSimilarComplaints(
                        complaint.getTitle(), complaint.getLatitude(), complaint.getLongitude())
                .stream()
                .filter(c -> !c.getId().equals(complaintId))
                .toList();
    }

    @Override
    public List<Complaint> checkPotentialDuplicates(String title, Double lat, Double lng) {
        return complaintRepository.findSimilarComplaints(title, lat, lng);
    }
}

