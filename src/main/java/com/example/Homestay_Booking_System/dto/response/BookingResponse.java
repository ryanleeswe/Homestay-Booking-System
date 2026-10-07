package com.example.Homestay_Booking_System.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.Homestay_Booking_System.domain.Booking;
import com.example.Homestay_Booking_System.domain.BookingStatus;
import com.example.Homestay_Booking_System.domain.PaymentStatus;

public record BookingResponse(Long id, Long userId, Long homestayId, String homestayName,
        Long roomId, LocalDate checkInDate, LocalDate checkOutDate, BigDecimal totalPrice,
        BookingStatus status, PaymentStatus paymentStatus) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getUser().getId(), booking.getHomestay().getId(),
                booking.getHomestay().getName(), booking.getRoom() == null ? null : booking.getRoom().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getTotalPrice(),
                booking.getStatus(), booking.getPaymentStatus());
    }
}
