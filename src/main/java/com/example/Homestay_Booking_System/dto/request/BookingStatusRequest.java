package com.example.Homestay_Booking_System.dto.request;

import com.example.Homestay_Booking_System.domain.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record BookingStatusRequest(@NotNull BookingStatus status) {}
