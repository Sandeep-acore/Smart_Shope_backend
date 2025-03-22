package com.smartshop.api.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Column(name = "path", nullable = false)
    private String filePath;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "data", columnDefinition = "BYTEA")
    private byte[] data;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    public FileData(String name, String fileName, String fileType, String filePath, byte[] data) {
        this.name = name;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.type = determineFileType(fileType);
        this.data = data;
    }
    
    public FileData(String fileName, String fileType, String filePath, byte[] data) {
        this.name = fileName;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.type = determineFileType(fileType);
        this.data = data;
    }
    
    private String determineFileType(String mimeType) {
        if (mimeType == null) {
            return "unknown";
        }
        if (mimeType.startsWith("image/")) {
            return "image";
        } else if (mimeType.startsWith("video/")) {
            return "video";
        } else if (mimeType.startsWith("audio/")) {
            return "audio";
        } else if (mimeType.equals("application/pdf")) {
            return "pdf";
        } else if (mimeType.contains("text/")) {
            return "text";
        } else if (mimeType.contains("application/msword") || 
                  mimeType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml")) {
            return "document";
        } else if (mimeType.contains("application/vnd.ms-excel") || 
                  mimeType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml")) {
            return "spreadsheet";
        } else {
            return "other";
        }
    }
} 