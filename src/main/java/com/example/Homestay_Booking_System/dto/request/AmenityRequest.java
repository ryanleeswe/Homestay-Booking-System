package com.example.Homestay_Booking_System.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AmenityRequest(@NotBlank String name) {
}
