package com.example.Homestay_Booking_System.dto.response;

import java.time.LocalDateTime;
import com.example.Homestay_Booking_System.domain.Review;

public record ReviewResponse(Long id, Long bookingId, int rating, String comment, LocalDateTime createdAt) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review.getId(), review.getBooking().getId(), review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
