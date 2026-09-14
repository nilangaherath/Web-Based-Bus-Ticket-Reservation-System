package com.busticket.reservation.strategy;

import com.busticket.reservation.entity.ComplaintCategory;
import com.busticket.reservation.entity.ComplaintPriority;
import org.springframework.stereotype.Component;

/**
 * Strategy for General suggestions, route inquiries, and portal feedback.
 * Handled as LOW priority with a 48-hour SLA.
 */
@Component
public class GeneralFeedbackSlaStrategy implements ComplaintSlaStrategy {

    @Override
    public boolean supports(ComplaintCategory category) {
        return category == ComplaintCategory.GENERAL_FEEDBACK;
    }

    @Override
    public ComplaintPriority getPriority() {
        return ComplaintPriority.LOW;
    }

    @Override
    public int getResolutionTurnaroundHours() {
        return 48; // 48 Hours Turnaround
    }
}
