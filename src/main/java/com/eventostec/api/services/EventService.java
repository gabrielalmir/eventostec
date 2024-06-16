package com.eventostec.api.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;

@Service
public class EventService {
    @Value("${aws.bucket.name}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public EventService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public Event createEvent(EventRequestDTO request) {
        String imageUrl = null;

        if (request.image() != null) {
            imageUrl = this.uploadImage(request.image());
        }

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventUrl(request.eventUrl());
        event.setDate(new Date(request.date()));
        event.setRemote(request.remote());
        event.setImageUrl(imageUrl);

        return event;
    }

    private String uploadImage(MultipartFile mfile) {
        String filename = UUID.randomUUID() + "-" + mfile.getOriginalFilename();

        try {
            File file = this.convertMultipartToFile(mfile);
            amazonS3.putObject(bucketName, filename, file);
            file.delete();
            return amazonS3.getUrl(bucketName, filename).toString();
        } catch (Exception e) {
            System.out.println("Error on uploading the file");
        }

        return null;
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        return file;
    }
}
