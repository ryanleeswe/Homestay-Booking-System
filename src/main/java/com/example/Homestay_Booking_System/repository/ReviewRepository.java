package com.example.Homestay_Booking_System.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Homestay_Booking_System.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);
}
