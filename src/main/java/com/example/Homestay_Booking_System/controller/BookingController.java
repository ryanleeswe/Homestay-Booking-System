package com.example.Homestay_Booking_System.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.domain.Booking;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.dto.request.BookingRequest;
import com.example.Homestay_Booking_System.dto.request.BookingStatusRequest;
import com.example.Homestay_Booking_System.dto.response.BookingResponse;
import com.example.Homestay_Booking_System.service.BookingService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Tạo và quản lý đặt phòng")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @GetMapping
    @Operation(summary = "Xem tất cả lượt đặt phòng")
    public List<BookingResponse> getAll() { return bookingService.getAll().stream().map(BookingResponse::from).toList(); }

    @GetMapping("/{id}")
    @Operation(summary = "Xem lượt đặt phòng theo ID")
    public BookingResponse getById(@PathVariable Long id) { return BookingResponse.from(bookingService.getById(id)); }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Xem lượt đặt phòng của user")
    public List<BookingResponse> getByUser(@PathVariable Long userId) { return bookingService.getByUser(userId).stream().map(BookingResponse::from).toList(); }

    @GetMapping("/homestay/{homestayId}")
    @Operation(summary = "Xem lượt đặt phòng của homestay")
    public List<BookingResponse> getByHomestay(@PathVariable Long homestayId) { return bookingService.getByHomestay(homestayId).stream().map(BookingResponse::from).toList(); }

    @PostMapping
    @Operation(summary = "Tạo lượt đặt phòng")
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(bookingService.create(toEntity(request))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lượt đặt phòng")
    public BookingResponse update(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return BookingResponse.from(bookingService.update(id, toEntity(request)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái đặt phòng")
    public BookingResponse changeStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusRequest request) {
        return BookingResponse.from(bookingService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa lượt đặt phòng")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return bookingService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private Booking toEntity(BookingRequest request) {
        User user = new User();
        user.setId(request.userId());
        Homestay homestay = new Homestay();
        homestay.setId(request.homestayId());
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setHomestay(homestay);
        if (request.roomId() != null) {
            com.example.Homestay_Booking_System.domain.Room room = new com.example.Homestay_Booking_System.domain.Room();
            room.setId(request.roomId());
            booking.setRoom(room);
        }
        booking.setCheckInDate(request.checkInDate());
        booking.setCheckOutDate(request.checkOutDate());
        return booking;
    }
}
