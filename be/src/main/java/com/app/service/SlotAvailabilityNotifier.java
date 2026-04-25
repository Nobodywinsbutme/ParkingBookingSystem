package com.app.service;

import com.app.dto.parking.SlotTopicMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Pushes lightweight notifications so browsers refresh slot availability (STOMP /topic/slots).
 */
@Component
public class SlotAvailabilityNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public SlotAvailabilityNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyAreaChanged(String parkingAreaId) {
        if (parkingAreaId == null || parkingAreaId.isBlank()) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/slots", new SlotTopicMessage(parkingAreaId, "BOOKING_CHANGED"));
    }
}
