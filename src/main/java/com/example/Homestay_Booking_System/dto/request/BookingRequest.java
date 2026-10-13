package com.example.Homestay_Booking_System.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record BookingRequest(
        @NotNull Long userId,
        @NotNull Long homestayId,
        Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        @Min(1) Integer numGuests) {}
