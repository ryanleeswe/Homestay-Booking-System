package com.example.Homestay_Booking_System.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Homestay_Booking_System.domain.Booking;
import com.example.Homestay_Booking_System.domain.BookingStatus;
import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.domain.Room;
import com.example.Homestay_Booking_System.repository.BookingRepository;
import com.example.Homestay_Booking_System.repository.HomestayRepository;
import com.example.Homestay_Booking_System.repository.UserRepository;
import com.example.Homestay_Booking_System.repository.RoomRepository;

@Service
public class BookingService {
    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    private final BookingRepository bookingRepository;
    private final HomestayRepository homestayRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookings, HomestayRepository homestays, UserRepository users, RoomRepository rooms) {
        this.bookingRepository = bookings;
        this.homestayRepository = homestays;
        this.userRepository = users;
        this.roomRepository = rooms;
    }

    public List<Booking> getAll() { return bookingRepository.findAll(); }
    public List<Booking> getByUser(Long userId) { return bookingRepository.findByUserId(userId); }
    public List<Booking> getByHomestay(Long homestayId) { return bookingRepository.findByHomestayId(homestayId); }
    public Booking getById(Long id) { return bookingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found")); }

    @Transactional
    public Booking create(Booking request) {
        Booking booking = new Booking();
        booking.setUser(resolveUser(request.getUser()));
        booking.setHomestay(resolveHomestay(request.getHomestay()));
        booking.setRoom(resolveRoom(request.getRoom(), booking.getHomestay()));
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.PENDING);
        validateDates(booking);
        ensureAvailable(booking, null);
        booking.setTotalPrice(calculatePrice(booking));
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking update(Long id, Booking request) {
        Booking booking = getById(id);
        if (booking.getStatus() != BookingStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending bookings can be edited");
        booking.setUser(resolveUser(request.getUser()));
        booking.setHomestay(resolveHomestay(request.getHomestay()));
        booking.setRoom(resolveRoom(request.getRoom(), booking.getHomestay()));
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        validateDates(booking);
        ensureAvailable(booking, id);
        booking.setTotalPrice(calculatePrice(booking));
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking changeStatus(Long id, BookingStatus status) {
        Booking booking = getById(id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already finalized");
        if (status == null || status == BookingStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A final booking status is required");
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public boolean delete(Long id) {
        if (!bookingRepository.existsById(id)) return false;
        bookingRepository.deleteById(id);
        return true;
    }

    private User resolveUser(User reference) {
        if (reference == null || reference.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user.id is required");
        return userRepository.findById(reference.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Homestay resolveHomestay(Homestay reference) {
        if (reference == null || reference.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "homestay.id is required");
        Homestay result = homestayRepository.findById(reference.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Homestay not found"));
        if (!result.isAvailable()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Homestay is unavailable");
        return result;
    }

    private Room resolveRoom(Room reference, Homestay homestay) {
        if (reference == null) return null;
        if (reference.getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "room.id is required");
        Room room = roomRepository.findById(reference.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        if (!room.getHomestay().getId().equals(homestay.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room does not belong to the selected homestay");
        return room;
    }

    private void validateDates(Booking booking) {
        LocalDate in = booking.getCheckInDate(), out = booking.getCheckOutDate();
        if (in == null || out == null || !in.isAfter(LocalDate.now()) || !out.isAfter(in))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Check-in must be in the future and check-out after check-in");
    }

    private void ensureAvailable(Booking booking, Long ignoredId) {
        Long requestedRoomId = booking.getRoom() == null ? null : booking.getRoom().getId();
        boolean conflict = bookingRepository.findByHomestayId(booking.getHomestay().getId()).stream()
                .filter(other -> ignoredId == null || !other.getId().equals(ignoredId))
                // A booking without a room occupies the whole homestay (legacy API).
                // Otherwise only bookings for the same room conflict.
                .filter(other -> requestedRoomId == null
                        || other.getRoom() == null
                        || requestedRoomId.equals(other.getRoom().getId()))
                .filter(other -> BLOCKING_STATUSES.contains(other.getStatus()))
                .anyMatch(other -> other.getCheckInDate().isBefore(booking.getCheckOutDate())
                        && other.getCheckOutDate().isAfter(booking.getCheckInDate()));
        if (conflict) throw new ResponseStatusException(HttpStatus.CONFLICT, "Homestay is already booked for those dates");
    }

    private BigDecimal calculatePrice(Booking booking) {
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal nightlyPrice = booking.getRoom() == null
                ? booking.getHomestay().getPrice()
                : booking.getRoom().getPricePerNight();
        return nightlyPrice.multiply(BigDecimal.valueOf(nights));
    }
}
