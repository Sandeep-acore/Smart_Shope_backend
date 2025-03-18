package com.smartshop.api.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStoragePath;

    @PostConstruct
    public void init() {
        try {
            fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(fileStoragePath);
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", e.getMessage());
        }
    }

    public String storeFile(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            // Create subdirectory if needed
            Path targetDir = fileStoragePath.resolve(directory);
            Files.createDirectories(targetDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            // Store the file
            Path targetLocation = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return directory + "/" + newFilename;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", e.getMessage());
            return null;
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path targetLocation = fileStoragePath.resolve(filePath).normalize();
            Resource resource = new UrlResource(targetLocation.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                logger.error("File not found or not readable: {}", filePath);
                throw new RuntimeException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL: {}", e.getMessage());
            throw new RuntimeException("Malformed URL: " + e.getMessage());
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            Path targetLocation = fileStoragePath.resolve(filePath);
            return Files.deleteIfExists(targetLocation);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
    }
} 