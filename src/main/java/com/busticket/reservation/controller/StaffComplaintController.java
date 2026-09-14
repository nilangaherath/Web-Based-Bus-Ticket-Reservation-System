package com.busticket.reservation.controller;

import com.busticket.reservation.dto.StaffResolutionDTO;
import com.busticket.reservation.entity.Complaint;
import com.busticket.reservation.entity.ComplaintCategory;
import com.busticket.reservation.entity.ComplaintPriority;
import com.busticket.reservation.entity.ComplaintStatus;
import com.busticket.reservation.service.ComplaintService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/complaints")
public class StaffComplaintController {

    private static final Logger log = LoggerFactory.getLogger(StaffComplaintController.class);

    private final ComplaintService complaintService;
    private static final Long DEFAULT_STAFF_ID = 3L;

    public StaffComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public String showStaffDashboard(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Complaint> complaintsPage = complaintService.searchComplaints(status, priority, category, keyword, pageable);

        model.addAttribute("complaintsPage", complaintsPage);
        model.addAttribute("complaints", complaintsPage.getContent());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);

        model.addAttribute("allStatuses", ComplaintStatus.values());
        model.addAttribute("allPriorities", ComplaintPriority.values());
        model.addAttribute("allCategories", ComplaintCategory.values());

        model.addAttribute("countOpen", complaintService.getCountByStatus(ComplaintStatus.OPEN));
        model.addAttribute("countInProgress", complaintService.getCountByStatus(ComplaintStatus.IN_PROGRESS));
        model.addAttribute("countResolved", complaintService.getCountByStatus(ComplaintStatus.RESOLVED));

        return "complaints/manage-complaints";
    }

    @GetMapping("/respond/{ticketId}")
    public String showRespondForm(@PathVariable String ticketId, Model model) {
        Complaint complaint = complaintService.getComplaintByTicketId(ticketId);

        StaffResolutionDTO dto = StaffResolutionDTO.builder()
                .ticketId(complaint.getTicketId())
                .status(complaint.getStatus())
                .priority(complaint.getPriority())
                .staffRemarks(complaint.getStaffRemarks())
                .build();

        model.addAttribute("complaint", complaint);
        model.addAttribute("resolutionDTO", dto);
        model.addAttribute("allStatuses", ComplaintStatus.values());
        model.addAttribute("allPriorities", ComplaintPriority.values());

        return "complaints/respond-complaint";
    }

    @PostMapping("/respond/{ticketId}")
    public String submitResolution(
            @PathVariable String ticketId,
            @Valid @ModelAttribute("resolutionDTO") StaffResolutionDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Complaint complaint = complaintService.getComplaintByTicketId(ticketId);
            model.addAttribute("complaint", complaint);
            model.addAttribute("allStatuses", ComplaintStatus.values());
            model.addAttribute("allPriorities", ComplaintPriority.values());
            return "complaints/respond-complaint";
        }

        try {
            dto.setTicketId(ticketId);
            complaintService.resolveComplaint(dto, DEFAULT_STAFF_ID);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket " + ticketId + " has been successfully updated to " + dto.getStatus().getDisplayName() + "!");
            return "redirect:/staff/complaints";
        } catch (Exception ex) {
            Complaint complaint = complaintService.getComplaintByTicketId(ticketId);
            model.addAttribute("complaint", complaint);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("allStatuses", ComplaintStatus.values());
            model.addAttribute("allPriorities", ComplaintPriority.values());
            return "complaints/respond-complaint";
        }
    }

    @PostMapping("/delete/{ticketId}")
    public String deleteComplaint(@PathVariable String ticketId, RedirectAttributes redirectAttributes) {
        try {
            complaintService.deleteComplaintByStaff(ticketId);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket " + ticketId + " has been permanently deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete ticket: " + ex.getMessage());
        }
        return "redirect:/staff/complaints";
    }
}
