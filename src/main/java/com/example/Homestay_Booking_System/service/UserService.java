package com.example.Homestay_Booking_System.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.Homestay_Booking_System.domain.User;
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

    public User createUser(User user) {
        if (isEmailExists(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        user.setId(null);
        return userRepository.save(user);
    }

    public User updateUser(Long id, User changes) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        User sameEmailUser = userRepository.findByEmail(changes.getEmail());
        if (sameEmailUser != null && !sameEmailUser.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        existing.setName(changes.getName());
        existing.setEmail(changes.getEmail());
        existing.setPassword(changes.getPassword());
        return userRepository.save(existing);
    }

    public void deleteUserById(Long id) {
        this.userRepository.deleteById(id);
    }

}
