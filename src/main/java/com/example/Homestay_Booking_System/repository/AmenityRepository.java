package com.example.Homestay_Booking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Homestay_Booking_System.domain.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}
