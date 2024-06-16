package com.eventostec.api.domain.event;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequestDTO(
    @NotNull @Size(min=1, max=100)
    String title,
    @Size(max=250)
    String description,
    @NotNull
    Long date,
    @NotNull @Size(min = 1, max = 100)
    String city,
    @NotNull @Size(min = 1, max = 2)
    String uf,
    Boolean remote,
    String eventUrl,
    MultipartFile image
) {}
