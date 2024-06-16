package com.eventostec.api.domain.event;

import java.util.Date;
import java.util.UUID;

public record EventResponseDTO(
    UUID id,
    String title,
    String description,
    Date date,
    String city,
    String state,
    Boolean remote,
    String eventUrl,
    String imageUrl
) {
    public EventResponseDTO(Event event) {
        this(event.getId(), event.getTitle(), event.getDescription(),
            event.getDate(), "", "", event.getRemote(), event.getEventUrl(), event.getImageUrl());
    }
}
