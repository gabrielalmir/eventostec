package com.eventostec.api.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {
    @Value("${aws.bucket.name}")
    private String bucketName;

    private final AmazonS3 amazonS3;
    private final EventRepository eventRepository;

    public EventService(AmazonS3 amazonS3, EventRepository eventRepository) {
        this.amazonS3 = amazonS3;
        this.eventRepository = eventRepository;
    }

    public Event createEvent(EventRequestDTO request) throws IOException {
        String imageUrl = null;

        if (request.image() != null) {
            imageUrl = this.uploadImage(request.image());
        }

        var event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventUrl(request.eventUrl());
        event.setDate(new Date(request.date()));
        event.setRemote(request.remote());
        event.setImageUrl(imageUrl);

        eventRepository.save(event);

        return event;
    }

    private String uploadImage(MultipartFile mfile) throws IOException {
        var filename = UUID.randomUUID() + "-" + mfile.getOriginalFilename();

        var file = this.convertMultipartToFile(mfile);
        amazonS3.putObject(bucketName, filename, file);
        file.delete();
        return amazonS3.getUrl(bucketName, filename).toString();
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        var file = new File(multipartFile.getOriginalFilename());
        var fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }

    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var eventsPage = this.eventRepository.findUpcomingEvents(new Date(), pageable);
        return eventsPage.map(event -> new EventResponseDTO(
            event.getId(), event.getTitle(), event.getDescription(), event.getDate(),
            event.getAddress() != null ? event.getAddress().getCity() : "",
            event.getAddress() != null ? event.getAddress().getUf() : "",
            event.getRemote(), event.getEventUrl(), event.getImageUrl())).toList();
    }

    public List<EventResponseDTO> getFilteredEvents(int page, int size, String title, String city, String uf, Date startDate, Date endDate) {
        title = title != null ? title : "";
        city = city != null ? city : "";
        uf = uf != null ? uf : "";
        startDate = startDate != null ? startDate : new Date(0);
        endDate = endDate != null ? endDate : new Date();
        var pageable = PageRequest.of(page, size);
        var eventsPage = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);

        return eventsPage.map(event -> new EventResponseDTO(
            event.getId(), event.getTitle(), event.getDescription(), event.getDate(),
            event.getAddress() != null ? event.getAddress().getCity() : "",
            event.getAddress() != null ? event.getAddress().getUf() : "",
            event.getRemote(), event.getEventUrl(), event.getImageUrl())).toList();
    }
}
