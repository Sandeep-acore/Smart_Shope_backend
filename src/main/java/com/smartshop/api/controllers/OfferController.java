package com.smartshop.api.controllers;

import com.smartshop.api.models.Offer;
import com.smartshop.api.payload.response.MessageResponse;
import com.smartshop.api.repositories.OfferRepository;
import com.smartshop.api.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/offers")
public class OfferController {
    
    @Autowired
    private OfferRepository offerRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        List<Offer> offers = offerRepository.findByActiveTrue();
        return ResponseEntity.ok(offers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found with id: " + id));
        return ResponseEntity.ok(offer);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createOffer(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("image") MultipartFile image) {
        
        // Validate image
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Please select an image for the offer"));
        }
        
        String imageType = image.getContentType();
        if (imageType == null || !(imageType.equals("image/jpeg") || imageType.equals("image/jpg") || imageType.equals("image/png"))) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Only JPEG, JPG, or PNG images are allowed"));
        }
        
        // Save image
        String imagePath = fileStorageService.storeFile(image, "offers");
        
        // Create offer
        Offer offer = new Offer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setImageUrl(imagePath);
        offer.setActive(true);
        
        offerRepository.save(offer);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOffer(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "active", required = false) Boolean active) {
        
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found with id: " + id));
        
        // Update image if provided
        if (image != null && !image.isEmpty()) {
            String imageType = image.getContentType();
            if (imageType == null || !(imageType.equals("image/jpeg") || imageType.equals("image/jpg") || imageType.equals("image/png"))) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Only JPEG, JPG, or PNG images are allowed"));
            }
            
            // Delete old image if exists
            if (offer.getImageUrl() != null) {
                fileStorageService.deleteFile(offer.getImageUrl());
            }
            
            // Save new image
            String imagePath = fileStorageService.storeFile(image, "offers");
            offer.setImageUrl(imagePath);
        }
        
        // Update other fields
        offer.setTitle(title);
        offer.setDescription(description);
        
        if (active != null) {
            offer.setActive(active);
        }
        
        offerRepository.save(offer);
        
        return ResponseEntity.ok(offer);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteOffer(@PathVariable Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Offer not found with id: " + id));
        
        // Delete image
        if (offer.getImageUrl() != null) {
            fileStorageService.deleteFile(offer.getImageUrl());
        }
        
        offerRepository.delete(offer);
        
        return ResponseEntity.ok(new MessageResponse("Offer deleted successfully"));
    }
    
    @GetMapping("/images")
    public ResponseEntity<List<String>> getAllOfferImages() {
        List<Offer> offers = offerRepository.findByActiveTrue();
        List<String> imageUrls = offers.stream()
                .map(Offer::getImageUrl)
                .collect(Collectors.toList());
        return ResponseEntity.ok(imageUrls);
    }
} 