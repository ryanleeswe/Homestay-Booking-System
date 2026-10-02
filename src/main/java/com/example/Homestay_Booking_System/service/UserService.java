package com.example.Homestay_Booking_System.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleCreateUser(User user) {

        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public User handleGetUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public User handleUpdateUser(User updatedUser) {
        User currentUser = this.handleGetUserById(updatedUser.getId());
        if (currentUser != null) {
            currentUser.setName(updatedUser.getName());
            currentUser.setPassword(updatedUser.getPassword());
            currentUser.setEmail(updatedUser.getEmail());

            return this.userRepository.save(currentUser);
        }
        return null;
    }

    public User findByEmail(String username) {
        return this.userRepository.findByEmail(username);

    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

}
