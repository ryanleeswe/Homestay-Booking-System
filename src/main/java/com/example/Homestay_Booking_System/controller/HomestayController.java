package com.example.Homestay_Booking_System.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.dto.request.HomestayRequest;
import com.example.Homestay_Booking_System.dto.response.HomestayResponse;
import com.example.Homestay_Booking_System.service.HomestayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/homestays")
public class HomestayController {

    private final HomestayService homestayService;

    public HomestayController(HomestayService homestayService) {
        this.homestayService = homestayService;
    }

    @GetMapping
    public ResponseEntity<List<HomestayResponse>> getAll() {
        return ResponseEntity.ok(homestayService.getAllHomestays().stream().map(HomestayResponse::from).toList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<HomestayResponse>> getAvailable() {
        return ResponseEntity.ok(homestayService.getAvailableHomestays().stream().map(HomestayResponse::from).toList());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<HomestayResponse>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(homestayService.getHomestaysByOwner(ownerId).stream().map(HomestayResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HomestayResponse> getById(@PathVariable Long id) {
        return homestayService.getHomestayById(id)
                .map(HomestayResponse::from).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HomestayResponse> create(@Valid @RequestBody HomestayRequest request) {
        Homestay homestay = toEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(HomestayResponse.from(homestayService.createHomestay(homestay, request.ownerId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HomestayResponse> update(@PathVariable Long id, @Valid @RequestBody HomestayRequest request) {
        return homestayService.updateHomestay(id, toEntity(request), request.ownerId()).map(HomestayResponse::from).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boolean isDeleted = homestayService.deleteHomestay(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Homestay not found with ID: " + id);
        }
        return ResponseEntity.ok("Homestay deleted successfully with ID: " + id);
    }

    private Homestay toEntity(HomestayRequest request) {
        Homestay homestay = new Homestay();
        homestay.setName(request.name());
        homestay.setLocation(request.location());
        homestay.setDescription(request.description());
        homestay.setPrice(request.price());
        homestay.setNumberOfGuests(request.numberOfGuests());
        homestay.setAvailable(request.available());
        return homestay;
    }
}
