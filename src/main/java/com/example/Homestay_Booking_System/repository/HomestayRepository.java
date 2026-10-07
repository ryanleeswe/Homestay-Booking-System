package com.example.Homestay_Booking_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.Homestay_Booking_System.domain.Homestay;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long>,JpaSpecificationExecutor<Homestay> {
    java.util.List<Homestay> findByAvailableTrue();
    java.util.List<Homestay> findByOwnerId(Long ownerId);
}
