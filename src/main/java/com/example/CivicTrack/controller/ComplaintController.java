package com.example.CivicTrack.controller;

import com.example.CivicTrack.dto.ComplaintRequestDTO;
import com.example.CivicTrack.dto.ComplaintResponseDTO;
import com.example.CivicTrack.dto.DashboardResponseDTO;
import com.example.CivicTrack.model.Category;
import com.example.CivicTrack.model.Complaint;
import com.example.CivicTrack.model.ComplaintLog;
import com.example.CivicTrack.service.ComplaintService;
import com.example.CivicTrack.service.UpvoteService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.CivicTrack.model.Status;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final UpvoteService upvoteService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ComplaintResponseDTO> createComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "area", required = false) String area,
            @RequestParam(value = "road", required = false) String road,
            @RequestParam(value = "pincode", required = false) String pincode,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {

        String email = (String) authentication.getPrincipal();

        ComplaintRequestDTO dto = new ComplaintRequestDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCategory(Category.valueOf(category));
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setArea(area);
        dto.setRoad(road);
        dto.setPincode(pincode);

        Complaint saved = complaintService.createComplaint(dto, files, email);

        return ResponseEntity.ok(complaintService.mapToDTO(saved));
    }

    @GetMapping
    public ResponseEntity<Page<ComplaintResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(
                complaintService.getAllComplaints(pageable)
                        .map(complaintService::mapToDTO)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ComplaintResponseDTO>> search(
            @RequestParam String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                complaintService.searchComplaints(keyword, pageable)
                        .map(complaintService::mapToDTO)
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ComplaintResponseDTO>> nearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam Double radius
    ) {
        return ResponseEntity.ok(
                complaintService.getNearbyComplaints(lat, lng, radius)
                        .stream()
                        .map(complaintService::mapToDTO)
                        .toList()
        );
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<String> upvote(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        String email = (String) authentication.getPrincipal();
        upvoteService.upvoteByEmail(email, id);

        return ResponseEntity.ok("Upvoted successfully");
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<ComplaintResponseDTO> verify(@PathVariable UUID id) {
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.verifyComplaint(id)
                )
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ComplaintResponseDTO> reject(
            @PathVariable UUID id,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.rejectComplaint(id, reason)
                )
        );
    }

    @PostMapping("/{id}/assign/{deptId}")
    public ResponseEntity<ComplaintResponseDTO> assign(
            @PathVariable UUID id,
            @PathVariable UUID deptId
    ) {
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.assignComplaint(id, deptId)
                )
        );
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ComplaintResponseDTO> start(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.startWork(id, email)
                )
        );
    }

//    @PostMapping("/{id}/complete")
//    public ResponseEntity<ComplaintResponseDTO> complete(
//            @PathVariable UUID id,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files
//    ) {
//        return ResponseEntity.ok(
//                complaintService.mapToDTO(
//                        complaintService.completeComplaint(id, files)
//                )
//        );
//    }

    @PostMapping("/{id}/done")
    public ResponseEntity<ComplaintResponseDTO> markDone(
            @PathVariable UUID id,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.markDone(id, files, email)
                )
        );
    }


    @PostMapping("/{id}/approve")
    public ResponseEntity<ComplaintResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.approveCompletion(id)
                )
        );
    }

    @PostMapping("/{id}/reject-after-done")
    public ResponseEntity<ComplaintResponseDTO> rejectAfterDone(
            @PathVariable UUID id,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(
                complaintService.mapToDTO(
                        complaintService.rejectAfterDone(id, reason)
                )
        );
    }

    @GetMapping("/department")
    public ResponseEntity<List<ComplaintResponseDTO>> getDepartmentComplaints(
            Authentication authentication
    ) {

        String email = (String) authentication.getPrincipal();

        return ResponseEntity.ok(
                complaintService.getComplaintsForDepartment(email)
                        .stream()
                        .map(complaintService::mapToDTO)
                        .toList()
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(complaintService.getDashboardStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponseDTO> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(complaintService.mapToDTO(complaintService.getComplaintById(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ComplaintLog>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(complaintService.getComplaintHistory(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ComplaintResponseDTO>> getMine(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(
                complaintService.getComplaintsForUser(email)
                        .stream()
                        .map(complaintService::mapToDTO)
                        .toList()
        );
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<ComplaintResponseDTO>> byStatus(@RequestParam Status status, Pageable pageable) {
        return ResponseEntity.ok(
                complaintService.getComplaintsByStatus(status, pageable).map(complaintService::mapToDTO)
        );
    }

    @GetMapping("/check-duplicates")
    public ResponseEntity<List<ComplaintResponseDTO>> checkDuplicates(
            @RequestParam String title,
            @RequestParam Double lat,
            @RequestParam Double lng
    ) {
        return ResponseEntity.ok(
                complaintService.checkPotentialDuplicates(title, lat, lng)
                        .stream().map(complaintService::mapToDTO).toList()
        );
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<ComplaintResponseDTO>> similar(@PathVariable UUID id) {
        return ResponseEntity.ok(
                complaintService.getSimilarComplaints(id).stream().map(complaintService::mapToDTO).toList()
        );
    }


}