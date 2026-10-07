package com.example.Homestay_Booking_System.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HomestayRequest(
        @NotNull Long ownerId,
        @NotBlank String name,
        @NotBlank String location,
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @Min(1) int numberOfGuests,
        boolean available) {}
