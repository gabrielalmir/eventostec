package com.eventostec.api.domain.event;

import java.util.Date;
import java.util.UUID;

public record EventDetailsDTO(
    UUID id,
    String title,
    String description,
    String city,
    String uf,
    Boolean remote,
    Date date
) {}
