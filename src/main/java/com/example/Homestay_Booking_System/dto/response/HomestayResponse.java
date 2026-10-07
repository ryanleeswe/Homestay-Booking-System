package com.example.Homestay_Booking_System.dto.response;

import java.math.BigDecimal;

import com.example.Homestay_Booking_System.domain.Homestay;

public record HomestayResponse(Long id, Long ownerId, String ownerName, String name, String location, String description,
        BigDecimal price, int numberOfGuests, boolean available) {
    public static HomestayResponse from(Homestay homestay) {
        return new HomestayResponse(homestay.getId(), homestay.getOwner() == null ? null : homestay.getOwner().getId(),
                homestay.getOwner() == null ? null : homestay.getOwner().getName(), homestay.getName(), homestay.getLocation(),
                homestay.getDescription(), homestay.getPrice(), homestay.getNumberOfGuests(), homestay.isAvailable());
    }
}
