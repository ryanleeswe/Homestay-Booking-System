package com.example.Homestay_Booking_System.service;

import org.springframework.stereotype.Service;

import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUserById(Long id) {
        this.userRepository.deleteById(id);
    }

}
