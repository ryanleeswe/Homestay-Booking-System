package com.example.Homestay_Booking_System.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.Homestay_Booking_System.domain.Booking;
import com.example.Homestay_Booking_System.domain.BookingStatus;
import com.example.Homestay_Booking_System.domain.Review;
import com.example.Homestay_Booking_System.dto.request.ReviewRequest;
import com.example.Homestay_Booking_System.dto.response.ReviewResponse;
import com.example.Homestay_Booking_System.repository.BookingRepository;
import com.example.Homestay_Booking_System.repository.ReviewRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/bookings/{bookingId}/review")
@Tag(name = "Reviews", description = "Tạo và xem đánh giá đặt phòng")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {
    private final BookingRepository bookings;
    private final ReviewRepository reviews;

    public ReviewController(BookingRepository bookings, ReviewRepository reviews) {
        this.bookings = bookings;
        this.reviews = reviews;
    }

    @PostMapping
    @Operation(summary = "Tạo đánh giá cho lượt đặt phòng đã hoàn thành")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long bookingId, @Valid @RequestBody ReviewRequest request) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (booking.getStatus() != BookingStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only completed bookings can be reviewed");
        if (reviews.existsByBookingId(bookingId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already has a review");
        Review review = new Review();
        review.setBooking(booking);
        review.setRating(request.rating());
        review.setComment(request.comment());
        booking.setReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(reviews.save(review)));
    }

    @GetMapping
    @Operation(summary = "Xem đánh giá của lượt đặt phòng")
    public ReviewResponse get(@PathVariable Long bookingId) {
        return ReviewResponse.from(reviews.findByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found")));
    }
}
