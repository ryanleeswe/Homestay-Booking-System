package com.example.Homestay_Booking_System.dto.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(@NotBlank String name, @Min(1) int maxGuests,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal pricePerNight) {
}
