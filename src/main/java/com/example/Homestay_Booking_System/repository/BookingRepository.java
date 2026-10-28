package com.example.Homestay_Booking_System.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Homestay_Booking_System.domain.Booking;
import com.example.Homestay_Booking_System.domain.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByHomestayId(Long homestayId);
    boolean existsByHomestayId(Long homestayId);
    boolean existsByHomestayIdAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            Long homestayId, List<BookingStatus> statuses, LocalDate checkOutDate, LocalDate checkInDate);
}
