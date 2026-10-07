package com.example.Homestay_Booking_System.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.repository.BookingRepository;
import com.example.Homestay_Booking_System.repository.HomestayRepository;
import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class HomestayService {
    private final HomestayRepository homestayRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public HomestayService(HomestayRepository homestayRepository, BookingRepository bookingRepository, UserRepository userRepository) {
        this.homestayRepository = homestayRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public List<Homestay> getAllHomestays() { return homestayRepository.findAll(); }
    public List<Homestay> getAvailableHomestays() { return homestayRepository.findByAvailableTrue(); }
    public List<Homestay> getHomestaysByOwner(Long ownerId) {
        findOwner(ownerId);
        return homestayRepository.findByOwnerId(ownerId);
    }
    public Optional<Homestay> getHomestayById(Long id) { return homestayRepository.findById(id); }
    @Transactional
    public Homestay createHomestay(Homestay homestay, Long ownerId) {
        homestay.setId(null);
        homestay.setOwner(findOwner(ownerId));
        return homestayRepository.save(homestay);
    }

    @Transactional
    public Optional<Homestay> updateHomestay(Long id, Homestay details, Long ownerId) {
        return homestayRepository.findById(id).map(existing -> {
            existing.setOwner(findOwner(ownerId));
            existing.setName(details.getName());
            existing.setLocation(details.getLocation());
            existing.setDescription(details.getDescription());
            existing.setPrice(details.getPrice());
            existing.setNumberOfGuests(details.getNumberOfGuests());
            existing.setAvailable(details.isAvailable());
            return homestayRepository.save(existing);
        });
    }

    private User findOwner(Long ownerId) {
        if (ownerId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ownerId is required");
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found"));
    }

    @Transactional
    public boolean deleteHomestay(Long id) {
        if (!homestayRepository.existsById(id)) return false;
        if (bookingRepository.existsByHomestayId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Homestay has bookings and cannot be deleted");
        homestayRepository.deleteById(id);
        return true;
    }
}
