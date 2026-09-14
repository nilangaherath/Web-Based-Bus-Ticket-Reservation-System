package com.busticket.reservation.strategy;

import com.busticket.reservation.entity.ComplaintCategory;
import com.busticket.reservation.entity.ComplaintPriority;

import java.time.LocalDateTime;

/**
 * Strategy interface defining SLA calculation and priority determination
 * based on the nature of the customer complaint.
 * 
 * Part of GoF Strategy Design Pattern implementation (Open/Closed Principle).
 */
public interface ComplaintSlaStrategy {

    /**
     * Determines whether this strategy supports the given category.
     */
    boolean supports(ComplaintCategory category);

    /**
     * Returns the recommended priority level for this category.
     */
    ComplaintPriority getPriority();

    /**
     * Returns resolution SLA turnaround time in hours.
     */
    int getResolutionTurnaroundHours();

    /**
     * Calculates the exact SLA resolution deadline starting from the submission time.
     */
    default LocalDateTime calculateSlaDeadline(LocalDateTime submissionTime) {
        return submissionTime.plusHours(getResolutionTurnaroundHours());
    }
}
