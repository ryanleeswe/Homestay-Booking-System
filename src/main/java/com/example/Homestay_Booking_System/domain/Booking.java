package com.example.Homestay_Booking_System.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

<<<<<<< HEAD
=======
/** Minimal entity for the room relationship; booking workflow is a separate feature. */
>>>>>>> fe34ed422146e3afa2a9f50887dedd31db236566
@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ nhiều đơn đặt phòng thuộc về một User
    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ nhiều đơn đặt phòng thuộc về một Homestay
    @NotNull(message = "Homestay is required")
    @ManyToOne
    @JoinColumn(name = "homestay_id", nullable = false)
    private Homestay homestay;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @NotNull(message = "Check-in date is required")
    @Column(nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Column(nullable = false)
    private LocalDate checkOutDate;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private int numGuests = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @jakarta.persistence.OneToOne(mappedBy = "booking", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Review review;

    @jakarta.persistence.PrePersist
    private void setCreatedAtIfMissing() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public BigDecimal calculateTotal() {
        if (checkInDate == null || checkOutDate == null) return BigDecimal.ZERO;
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal nightlyRate = room != null ? room.getPricePerNight() : homestay.getPrice();
        return nightlyRate.multiply(BigDecimal.valueOf(nights));
    }

    public void confirm() { this.status = BookingStatus.CONFIRMED; }
    public void cancel() { this.status = BookingStatus.CANCELLED; }
}
