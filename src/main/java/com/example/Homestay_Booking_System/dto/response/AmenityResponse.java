package com.example.Homestay_Booking_System.dto.response;

import com.example.Homestay_Booking_System.domain.Amenity;

public record AmenityResponse(Long id, String name) {
    public static AmenityResponse from(Amenity amenity) { return new AmenityResponse(amenity.getId(), amenity.getName()); }
}
