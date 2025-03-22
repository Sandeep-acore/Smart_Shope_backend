package com.smartshop.api.services;

import com.smartshop.api.models.FileData;
import com.smartshop.api.repositories.FileDataRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private FileDataRepository fileDataRepository;

    @PostConstruct
    public void init() {
        // No need to create physical directories anymore as we're storing in the database
        logger.info("Initialized file storage service for database storage");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String storeFile(MultipartFile file, String directory) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("Attempted to store empty or null file");
                return null;
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "unknown-file";
            }
            
            String extension = FilenameUtils.getExtension(originalFilename);
            if (extension == null || extension.isEmpty()) {
                extension = "bin";
            }
            
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            // Create file path
            String filePath = directory + "/" + newFilename;
            
            // Read file bytes
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                logger.error("Failed to read file bytes: {}", e.getMessage(), e);
                return null;
            }
            
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Create and save file data using constructor instead of setters
            FileData fileData = new FileData(
                originalFilename,  // name
                newFilename,       // fileName
                contentType,       // fileType
                filePath,          // filePath
                fileBytes          // data
            );
            
            try {
                saveFileData(fileData);
                logger.info("Successfully stored file in database with path: {}", filePath);
                return filePath;
            } catch (Exception e) {
                logger.error("Database error storing file: {}", e.getMessage(), e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Unexpected error storing file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected FileData saveFileData(FileData fileData) {
        return fileDataRepository.save(fileData);
    }

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Resource loadFileAsResource(String filePath) {
        try {
            // Load from database
            return getFileResource(filePath);
        } catch (Exception e) {
            logger.error("Error loading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading file: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    protected Resource getFileResource(String filePath) {
        return fileDataRepository.findByFilePath(filePath)
            .map(fileData -> {
                byte[] data = fileData.getData();
                if (data == null || data.length == 0) {
                    logger.error("File data is empty for path: {}", filePath);
                    throw new RuntimeException("File data is empty: " + filePath);
                }
                logger.info("Successfully loaded file from database: {}, size: {} bytes", filePath, data.length);
                return new ByteArrayResource(data);
            })
            .orElseThrow(() -> {
                logger.error("File not found in database: {}", filePath);
                return new RuntimeException("File not found: " + filePath);
            });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }
            
            return performDeleteFile(filePath);
        } catch (Exception e) {
            logger.error("Failed to delete file from database: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected boolean performDeleteFile(String filePath) {
        return fileDataRepository.findByFilePath(filePath)
            .map(fileData -> {
                try {
                    fileDataRepository.delete(fileData);
                    logger.info("Successfully deleted file from database: {}", filePath);
                    return true;
                } catch (Exception e) {
                    logger.error("Error deleting file: {}", e.getMessage(), e);
                    return false;
                }
            })
            .orElse(false);
    }
    
    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/files/")
            .path(filePath)
            .toUriString();
    }
} 