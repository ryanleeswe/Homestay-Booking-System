package com.example.Homestay_Booking_System.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.Homestay_Booking_System.domain.Amenity;
import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.dto.request.AmenityRequest;
import com.example.Homestay_Booking_System.dto.response.AmenityResponse;
import com.example.Homestay_Booking_System.repository.AmenityRepository;
import com.example.Homestay_Booking_System.repository.HomestayRepository;
import jakarta.validation.Valid;

@RestController
public class AmenityController {
    private final AmenityRepository amenities;
    private final HomestayRepository homestays;

    public AmenityController(AmenityRepository amenities, HomestayRepository homestays) {
        this.amenities = amenities;
        this.homestays = homestays;
    }

    @PostMapping("/amenities")
    public ResponseEntity<AmenityResponse> create(@Valid @RequestBody AmenityRequest request) {
        Amenity amenity = new Amenity();
        amenity.setName(request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(AmenityResponse.from(amenities.save(amenity)));
    }

    @PostMapping("/homestays/{homestayId}/amenities/{amenityId}")
    public ResponseEntity<Void> attach(@PathVariable Long homestayId, @PathVariable Long amenityId) {
        Homestay homestay = homestays.findById(homestayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Homestay not found"));
        Amenity amenity = amenities.findById(amenityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Amenity not found"));
        if (homestay.getAmenities().stream().noneMatch(item -> item.getId().equals(amenityId)))
            homestay.getAmenities().add(amenity);
        homestays.save(homestay);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/homestays/{homestayId}/amenities")
    public List<AmenityResponse> listForHomestay(@PathVariable Long homestayId) {
        Homestay homestay = homestays.findById(homestayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Homestay not found"));
        return homestay.getAmenities().stream().map(AmenityResponse::from).toList();
    }
}
