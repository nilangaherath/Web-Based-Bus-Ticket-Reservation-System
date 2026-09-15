package com.busticket.reservation.service;

import com.busticket.reservation.dto.ComplaintRequestDTO;
import com.busticket.reservation.dto.StaffResolutionDTO;
import com.busticket.reservation.entity.*;
import com.busticket.reservation.repository.BookingRepository;
import com.busticket.reservation.repository.ComplaintRepository;
import com.busticket.reservation.repository.UserRepository;
import com.busticket.reservation.strategy.ComplaintSlaStrategy;
import com.busticket.reservation.strategy.SlaStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SlaStrategyFactory slaStrategyFactory;
    private final SecureRandom secureRandom = new SecureRandom();

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                UserRepository userRepository,
                                BookingRepository bookingRepository,
                                SlaStrategyFactory slaStrategyFactory) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.slaStrategyFactory = slaStrategyFactory;
    }

    @Override
    @Transactional
    public Complaint createComplaint(ComplaintRequestDTO dto, Long userId) {
        log.info("Creating complaint for user ID: {} with category: {}", userId, dto.getCategory());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer user not found with ID: " + userId));

        Booking booking = null;
        if (dto.getBookingReference() != null && !dto.getBookingReference().trim().isEmpty()) {
            booking = bookingRepository.findByBookingReference(dto.getBookingReference().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Booking reference '" + dto.getBookingReference() + "' was not found."));
            
            if (!booking.getCustomer().getId().equals(userId)) {
                throw new IllegalArgumentException("The specified booking reference does not belong to your account.");
            }
        }

        ComplaintSlaStrategy strategy = slaStrategyFactory.getStrategy(dto.getCategory());
        ComplaintPriority priority = strategy.getPriority();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = strategy.calculateSlaDeadline(now);

        String ticketId = generateUniqueTicketId();

        Complaint complaint = Complaint.builder()
                .ticketId(ticketId)
                .user(user)
                .booking(booking)
                .category(dto.getCategory())
                .priority(priority)
                .status(ComplaintStatus.OPEN)
                .subject(dto.getSubject().trim())
                .description(dto.getDescription().trim())
                .slaDeadline(slaDeadline)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        log.info("Complaint created successfully. Ticket ID: {}, Priority: {}, SLA Deadline: {}",
                saved.getTicketId(), saved.getPriority(), saved.getSlaDeadline());
        return saved;
    }

    @Override
    @Transactional
    public Complaint updateComplaint(String ticketId, ComplaintRequestDTO dto, Long userId) {
        log.info("Updating complaint Ticket ID: {} by user ID: {}", ticketId, userId);

        Complaint complaint = getComplaintByTicketId(ticketId);

        if (!complaint.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized: You can only edit your own complaints.");
        }

        if (!complaint.isEditableByCustomer()) {
            throw new IllegalStateException("Ticket cannot be modified once it is in progress or resolved.");
        }

        Booking booking = null;
        if (dto.getBookingReference() != null && !dto.getBookingReference().trim().isEmpty()) {
            booking = bookingRepository.findByBookingReference(dto.getBookingReference().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Booking reference '" + dto.getBookingReference() + "' was not found."));
            if (!booking.getCustomer().getId().equals(userId)) {
                throw new IllegalArgumentException("The booking reference does not belong to your account.");
            }
        }

        if (complaint.getCategory() != dto.getCategory()) {
            ComplaintSlaStrategy strategy = slaStrategyFactory.getStrategy(dto.getCategory());
            complaint.setCategory(dto.getCategory());
            complaint.setPriority(strategy.getPriority());
            complaint.setSlaDeadline(strategy.calculateSlaDeadline(complaint.getCreatedAt()));
        }

        complaint.setSubject(dto.getSubject().trim());
        complaint.setDescription(dto.getDescription().trim());
        complaint.setBooking(booking);

        return complaintRepository.save(complaint);
    }

    @Override
    @Transactional
    public void deleteComplaintByCustomer(String ticketId, Long userId) {
        log.info("Customer ID: {} requested deletion of Ticket ID: {}", userId, ticketId);
        Complaint complaint = getComplaintByTicketId(ticketId);

        if (!complaint.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized: You can only cancel your own complaints.");
        }

        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new IllegalStateException("Only complaints in OPEN status can be cancelled or deleted by customers.");
        }

        complaintRepository.delete(complaint);
        log.info("Ticket ID: {} deleted by customer.", ticketId);
    }

    @Override
    @Transactional
    public void deleteComplaintByStaff(String ticketId) {
        log.info("Staff deletion of Ticket ID: {}", ticketId);
        Complaint complaint = getComplaintByTicketId(ticketId);
        complaintRepository.delete(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintByTicketId(String ticketId) {
        return complaintRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint ticket not found with ID: " + ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return complaintRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Complaint> searchComplaints(ComplaintStatus status, ComplaintPriority priority,
                                            ComplaintCategory category, String keyword, Pageable pageable) {
        return complaintRepository.searchAndFilterComplaints(status, priority, category, keyword, pageable);
    }

    @Override
    @Transactional
    public Complaint resolveComplaint(StaffResolutionDTO dto, Long staffId) {
        log.info("Staff ID: {} resolving Ticket ID: {} to status: {}", staffId, dto.getTicketId(), dto.getStatus());

        Complaint complaint = getComplaintByTicketId(dto.getTicketId());
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Support staff member not found with ID: " + staffId));

        complaint.setStatus(dto.getStatus());
        complaint.setPriority(dto.getPriority());
        complaint.setStaffRemarks(dto.getStaffRemarks().trim());

        if (dto.getStatus() == ComplaintStatus.RESOLVED || dto.getStatus() == ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
            complaint.setResolvedBy(staff);
        } else {
            complaint.setResolvedAt(null);
            complaint.setResolvedBy(null);
        }

        return complaintRepository.save(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountByStatus(ComplaintStatus status) {
        return complaintRepository.countByStatus(status);
    }

    private String generateUniqueTicketId() {
        String ticketId;
        do {
            int randomNum = 1000 + secureRandom.nextInt(9000);
            ticketId = String.format("TKT-%04d-2026", randomNum);
        } while (complaintRepository.findByTicketId(ticketId).isPresent());
        return ticketId;
    }
}
