package com.example.CivicTrack.service;

import com.example.CivicTrack.Repository.ComplaintLogRepository;
import com.example.CivicTrack.Repository.ComplaintRepository;
import com.example.CivicTrack.model.Complaint;
import com.example.CivicTrack.model.ComplaintLog;
import com.example.CivicTrack.model.Status;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalationService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintLogRepository logRepository;

    private static final List<Status> CLOSED_STATUSES = List.of(Status.COMPLETED, Status.REJECTED);

    /** Runs at the top of every hour. */
    @Scheduled(cron = "0 0 * * * *")
    public void checkOverdueComplaints() {
        runEscalationCheck();
    }

    @Transactional
    public int runEscalationCheck() {
        List<Complaint> overdue = complaintRepository
                .findByDueDateBeforeAndEscalatedFalseAndStatusNotIn(LocalDateTime.now(), CLOSED_STATUSES);

        for (Complaint complaint : overdue) {
            complaint.setEscalated(true);
            complaint.setEscalatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            logRepository.save(ComplaintLog.builder()
                    .complaint(complaint)
                    .action("ESCALATED")
                    .build());
        }

        return overdue.size();
    }
}