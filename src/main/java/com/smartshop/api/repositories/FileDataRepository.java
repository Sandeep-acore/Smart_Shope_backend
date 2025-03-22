package com.smartshop.api.repositories;

import com.smartshop.api.models.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileDataRepository extends JpaRepository<FileData, Long> {
    Optional<FileData> findByFilePath(String filePath);
} 