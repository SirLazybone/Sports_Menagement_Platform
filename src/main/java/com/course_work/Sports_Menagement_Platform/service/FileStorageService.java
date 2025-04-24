package com.course_work.Sports_Menagement_Platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path uploadDir = Paths.get("uploads");

    public FileStorageService() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            logger.info("Upload directory initialized at: {}", uploadDir.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            logger.info("File stored successfully: {}", filePath.toAbsolutePath());
            return fileName;
        } catch (IOException e) {
            logger.error("Could not store file", e);
            throw new RuntimeException("Could not store file", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = uploadDir.resolve(fileName);
            Files.deleteIfExists(filePath);
            logger.info("File deleted successfully: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not delete file", e);
            throw new RuntimeException("Could not delete file", e);
        }
    }
} 