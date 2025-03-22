package com.smartshop.api.services;

import com.smartshop.api.models.FileData;
import com.smartshop.api.repositories.FileDataRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DatabaseFileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFileStorageService.class);

    @Autowired
    private FileDataRepository fileDataRepository;

    public String storeFile(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                return null;
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            String filePath = directory + "/" + newFilename;
            String contentType = file.getContentType();

            // Store file in database
            FileData fileData = new FileData(
                    newFilename,
                    contentType,
                    filePath,
                    file.getBytes()
            );

            fileDataRepository.save(fileData);
            logger.info("File saved to database: {}", filePath);

            return filePath;
        } catch (IOException e) {
            logger.error("Failed to store file in database: {}", e.getMessage());
            return null;
        }
    }

    public FileData getFile(String filePath) {
        logger.info("Retrieving file from database: {}", filePath);
        FileData fileData = fileDataRepository.findByFilePath(filePath).orElse(null);
        if (fileData == null) {
            logger.warn("File not found in database: {}", filePath);
        } else {
            logger.info("File found in database: {}", filePath);
        }
        return fileData;
    }

    public boolean deleteFile(String filePath) {
        try {
            FileData fileData = fileDataRepository.findByFilePath(filePath).orElse(null);
            if (fileData != null) {
                fileDataRepository.delete(fileData);
                logger.info("File deleted from database: {}", filePath);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to delete file from database: {}", e.getMessage());
            return false;
        }
    }
} 