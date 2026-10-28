package com.example.Homestay_Booking_System.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(
        @NotNull Long id,
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password) {}
