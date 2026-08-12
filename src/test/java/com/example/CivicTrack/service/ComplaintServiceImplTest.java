//package com.example.CivicTrack.service;
//
//import com.example.CivicTrack.Repository.*;
//import com.example.CivicTrack.dto.ComplaintRequestDTO;
//import com.example.CivicTrack.model.*;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Covers the business rules that actually matter for CivicTrack:
// * status-transition guards, department-ownership enforcement, and
// * the validation checks applied when a citizen creates a complaint.
// *
// * Place this file at:
// * src/test/java/com/example/CivicTrack/service/ComplaintServiceImplTest.java
// */
//@ExtendWith(MockitoExtension.class)
//class ComplaintServiceImplTest {
//
//    @Mock private ComplaintRepository complaintRepository;
//    @Mock private DepartmentRepository departmentRepository;
//    @Mock private ComplaintLogRepository logRepository;
//    @Mock private UserRepository userRepository;
//    @Mock private FileStorageService fileStorageService;
//    @Mock private MediaRepository mediaRepository;
//    @Mock private UpvoteRepository upvoteRepository;
//
//    @InjectMocks
//    private ComplaintServiceImpl complaintService;
//
//    private User reportingUser;
//    private User departmentUser;
//    private Department department;
//    private Complaint complaint;
//
//    @BeforeEach
//    void setUp() {
//        reportingUser = User.builder()
//                .name("Resident")
//                .email("resident@example.com")
//                .role(Role.USER)
//                .build();
//
//        department = Department.builder()
//                .name("Roads Dept")
//                .build();
//
//        departmentUser = User.builder()
//                .name("Dept Worker")
//                .email("dept@example.com")
//                .role(Role.DEPARTMENT)
//                .department(department)
//                .build();
//
//        complaint = Complaint.builder()
//                .title("Pothole on MG Road")
//                .description("Large pothole causing traffic")
//                .category(Category.ROAD)
//                .latitude(19.0760)
//                .longitude(72.8777)
//                .status(Status.PENDING)
//                .user(reportingUser)
//                .build();
//    }
//
//    // ---------- verifyComplaint ----------
//
//    @Nested
//    class VerifyComplaint {
//
//        @Test
//        void verifiesAPendingComplaint() {
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.verifyComplaint(complaint.getId());
//
//            assertThat(result.getStatus()).isEqualTo(Status.VERIFIED);
//            verify(logRepository).save(any(ComplaintLog.class));
//        }
//
//        @Test
//        void rejectsVerifyingANonPendingComplaint() {
//            complaint.setStatus(Status.VERIFIED);
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//
//            assertThatThrownBy(() -> complaintService.verifyComplaint(complaint.getId()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("PENDING");
//
//            verify(complaintRepository, never()).save(any());
//        }
//    }
//
//    // ---------- assignComplaint ----------
//
//    @Nested
//    class AssignComplaint {
//
//        @Test
//        void assignsAVerifiedComplaintToADepartment() {
//            complaint.setStatus(Status.VERIFIED);
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.assignComplaint(complaint.getId(), department.getId());
//
//            assertThat(result.getStatus()).isEqualTo(Status.ASSIGNED);
//            assertThat(result.getDepartment()).isEqualTo(department);
//        }
//
//        @Test
//        void rejectsAssigningAnUnverifiedComplaint() {
//            // still PENDING
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//
//            assertThatThrownBy(() -> complaintService.assignComplaint(complaint.getId(), department.getId()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("VERIFIED");
//        }
//    }
//
//    // ---------- startWork: department ownership ----------
//
//    @Nested
//    class StartWork {
//
//        @Test
//        void startsWorkWhenCallerBelongsToTheAssignedDepartment() {
//            complaint.setStatus(Status.ASSIGNED);
//            complaint.setDepartment(department);
//
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(userRepository.findByEmail(departmentUser.getEmail())).thenReturn(Optional.of(departmentUser));
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.startWork(complaint.getId(), departmentUser.getEmail());
//
//            assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
//        }
//
//        @Test
//        void rejectsStartingWorkFromADifferentDepartment() {
//            complaint.setStatus(Status.ASSIGNED);
//            complaint.setDepartment(department);
//
//            Department otherDept = Department.builder().name("Water Dept").build();
//            User otherDeptUser = User.builder()
//                    .email("other@example.com")
//                    .role(Role.DEPARTMENT)
//                    .department(otherDept)
//                    .build();
//
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(userRepository.findByEmail(otherDeptUser.getEmail())).thenReturn(Optional.of(otherDeptUser));
//
//            assertThatThrownBy(() -> complaintService.startWork(complaint.getId(), otherDeptUser.getEmail()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("not authorized");
//
//            verify(complaintRepository, never()).save(any());
//        }
//
//        @Test
//        void rejectsStartingWorkOnAComplaintThatIsNotAssigned() {
//            complaint.setStatus(Status.VERIFIED);
//            complaint.setDepartment(department);
//
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(userRepository.findByEmail(departmentUser.getEmail())).thenReturn(Optional.of(departmentUser));
//
//            assertThatThrownBy(() -> complaintService.startWork(complaint.getId(), departmentUser.getEmail()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("ASSIGNED");
//        }
//    }
//
//    // ---------- approveCompletion / rejectAfterDone ----------
//
//    @Nested
//    class AuthorityReview {
//
//        @Test
//        void approvesADoneComplaint() {
//            complaint.setStatus(Status.DONE);
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.approveCompletion(complaint.getId());
//
//            assertThat(result.getStatus()).isEqualTo(Status.COMPLETED);
//        }
//
//        @Test
//        void rejectsApprovingAComplaintThatIsNotDone() {
//            complaint.setStatus(Status.IN_PROGRESS);
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//
//            assertThatThrownBy(() -> complaintService.approveCompletion(complaint.getId()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("DONE");
//        }
//
//        @Test
//        void sendsADoneComplaintBackToInProgressWithAReason() {
//            complaint.setStatus(Status.DONE);
//            when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.rejectAfterDone(complaint.getId(), "After-photos missing");
//
//            assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
//            assertThat(result.getRejectionReason()).isEqualTo("After-photos missing");
//        }
//    }
//
//    // ---------- createComplaint: validation rules ----------
//
//    @Nested
//    class CreateComplaint {
//
//        private ComplaintRequestDTO validDto() {
//            return ComplaintRequestDTO.builder()
//                    .title("Broken streetlight")
//                    .description("Not working for a week")
//                    .category(Category.ELECTRICITY)
//                    .latitude(19.0760)
//                    .longitude(72.8777)
//                    .build();
//        }
//
//        @Test
//        void rejectsCoordinatesOutsideIndia() {
//            when(userRepository.findByEmail(reportingUser.getEmail())).thenReturn(Optional.of(reportingUser));
//
//            ComplaintRequestDTO dto = validDto();
//            dto.setLatitude(51.5074); // London
//            dto.setLongitude(-0.1278);
//
//            assertThatThrownBy(() -> complaintService.createComplaint(dto, null, reportingUser.getEmail()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("Invalid India location");
//        }
//
//        @Test
//        void rejectsASixthComplaintInOneDay() {
//            when(userRepository.findByEmail(reportingUser.getEmail())).thenReturn(Optional.of(reportingUser));
//            when(complaintRepository.countByUserIdAndCreatedAtAfter(eq(reportingUser.getId()), any(LocalDateTime.class)))
//                    .thenReturn(5L);
//
//            assertThatThrownBy(() -> complaintService.createComplaint(validDto(), null, reportingUser.getEmail()))
//                    .isInstanceOf(RuntimeException.class)
//                    .hasMessageContaining("Daily complaint limit");
//
//            verify(complaintRepository, never()).save(any());
//        }
//
//        @Test
//        void createsAComplaintWhenValid() {
//            when(userRepository.findByEmail(reportingUser.getEmail())).thenReturn(Optional.of(reportingUser));
//            when(complaintRepository.countByUserIdAndCreatedAtAfter(eq(reportingUser.getId()), any(LocalDateTime.class)))
//                    .thenReturn(2L);
//            when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
//
//            Complaint result = complaintService.createComplaint(validDto(), List.of(), reportingUser.getEmail());
//
//            assertThat(result.getStatus()).isEqualTo(Status.PENDING);
//            assertThat(result.getUser()).isEqualTo(reportingUser);
//            verify(logRepository).save(any(ComplaintLog.class));
//        }
//    }
//}