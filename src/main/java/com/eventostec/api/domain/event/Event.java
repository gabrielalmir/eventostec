package com.eventostec.api.domain.event;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "event")
@Entity
@Data
public class Event {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String imageUrl;
    private String eventUrl;
    private Boolean remote;
    private Date date;
}
