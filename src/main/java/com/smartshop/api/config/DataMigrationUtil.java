package com.smartshop.api.config;

import com.smartshop.api.models.FileData;
import com.smartshop.api.repositories.FileDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataMigrationUtil implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMigrationUtil.class);
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Autowired
    private FileDataRepository fileDataRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data migration for image files...");
        migrateImagesToDatabase();
    }
    
    private void migrateImagesToDatabase() {
        // Directories to check for files
        String[] directories = {
            "products",
            "categories",
            "profiles",
            "offers"
        };
        
        for (String directory : directories) {
            Path dirPath = Paths.get(uploadDir, directory);
            File dirFile = dirPath.toFile();
            
            if (dirFile.exists() && dirFile.isDirectory()) {
                logger.info("Processing directory: {}", directory);
                
                File[] files = dirFile.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String filePath = directory + "/" + file.getName();
                            String fileName = file.getName();
                            
                            // Check if file already exists in database
                            if (!fileDataRepository.findByPath(filePath).isPresent()) {
                                try {
                                    String contentType = getContentType(fileName);
                                    byte[] data = Files.readAllBytes(file.toPath());
                                    
                                    FileData fileData = new FileData(
                                            fileName,
                                            contentType,
                                            filePath,
                                            data
                                    );
                                    
                                    // Save files individually to isolate issues
                                    fileDataRepository.save(fileData);
                                    logger.info("Successfully migrated file: {}", filePath);
                                } catch (Exception e) {
                                    logger.error("Error migrating file {}: {}", filePath, e.getMessage());
                                }
                            } else {
                                logger.info("File already exists in database: {}", filePath);
                            }
                        }
                    }
                }
            } else {
                logger.warn("Directory does not exist: {}", dirPath);
                // Create directory if it doesn't exist
                try {
                    Files.createDirectories(dirPath);
                    logger.info("Created directory: {}", dirPath);
                } catch (IOException e) {
                    logger.error("Error creating directory {}: {}", dirPath, e.getMessage());
                }
            }
        }
    }
    
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
} 