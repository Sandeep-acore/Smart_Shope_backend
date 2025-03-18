package com.smartshop.api.controllers;

import com.smartshop.api.models.Offer;
import com.smartshop.api.repositories.OfferRepository;
import com.smartshop.api.services.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private OfferRepository offerRepository;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            
            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }
            
            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error retrieving file: {}", fileName, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/offers/images")
    public ResponseEntity<List<String>> getAllOfferImageUrls() {
        List<Offer> offers = offerRepository.findByActiveTrue();
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<String> fullImageUrls = offers.stream()
                .map(offer -> {
                    String imageUrl = offer.getImageUrl();
                    return baseUrl + "/files/" + imageUrl;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(fullImageUrls);
    }
    
    @GetMapping("/offers/images/with-details")
    public ResponseEntity<List<OfferImageDTO>> getAllOfferImagesWithDetails() {
        List<Offer> offers = offerRepository.findByActiveTrue();
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        
        List<OfferImageDTO> offerImages = offers.stream()
                .map(offer -> {
                    String imageUrl = offer.getImageUrl();
                    return new OfferImageDTO(
                        offer.getId(),
                        offer.getTitle(),
                        baseUrl + "/files/" + imageUrl
                    );
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(offerImages);
    }
    
    // DTO for offer images with details
    static class OfferImageDTO {
        private Long id;
        private String title;
        private String imageUrl;
        
        public OfferImageDTO(Long id, String title, String imageUrl) {
            this.id = id;
            this.title = title;
            this.imageUrl = imageUrl;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
    }
} 