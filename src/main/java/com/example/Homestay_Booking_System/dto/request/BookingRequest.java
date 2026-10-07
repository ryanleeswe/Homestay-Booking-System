package com.example.Homestay_Booking_System.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull Long userId,
        @NotNull Long homestayId,
        Long roomId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate) {}
