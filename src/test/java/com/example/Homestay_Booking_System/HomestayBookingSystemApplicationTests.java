package com.example.Homestay_Booking_System;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.HomestayRepository;
import com.example.Homestay_Booking_System.repository.UserRepository;

@SpringBootTest
class HomestayBookingSystemApplicationTests {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HomestayRepository homestayRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void oneUserCanOwnMultipleHomestays() {
		User owner = new User();
		owner.setName("Relationship test owner");
		owner.setEmail("owner-" + UUID.randomUUID() + "@example.com");
		owner.setPassword("test-password");
		owner = userRepository.saveAndFlush(owner);

		homestayRepository.save(createHomestay(owner, "First homestay"));
		homestayRepository.save(createHomestay(owner, "Second homestay"));
		homestayRepository.flush();

		assertEquals(2, homestayRepository.findByOwnerId(owner.getId()).size());
	}

	private Homestay createHomestay(User owner, String name) {
		Homestay homestay = new Homestay();
		homestay.setOwner(owner);
		homestay.setName(name);
		homestay.setLocation("Da Lat");
		homestay.setDescription("Relationship test homestay");
		homestay.setPrice(new BigDecimal("500000.00"));
		homestay.setNumberOfGuests(2);
		homestay.setAvailable(true);
		return homestay;
	}
}
