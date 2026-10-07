package com.example.Homestay_Booking_System.dto.response;

import com.example.Homestay_Booking_System.domain.User;

public record UserResponse(Long id, String name, String email) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
