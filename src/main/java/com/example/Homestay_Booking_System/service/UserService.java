package com.example.Homestay_Booking_System.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User handleCreateUser(User user) {
        if (isEmailExist(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(com.example.Homestay_Booking_System.domain.Role.USER);
        return this.userRepository.save(user);
    }

    public void handleDeleteUser(long id) {
        User user = findUserOrThrow(id);
        assertOwnerOrAdmin(user);
        this.userRepository.delete(user);
    }

    public User handleGetUserById(long id) {
        User user = findUserOrThrow(id);
        assertOwnerOrAdmin(user);
        return user;
    }

    public User handleUpdateUser(User updatedUser) {
        User currentUser = findUserOrThrow(updatedUser.getId());
        assertOwnerOrAdmin(currentUser);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = hasAdminRole(authentication);
        if (!isAdmin && !Objects.equals(currentUser.getEmail(), updatedUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Users cannot change their email through this operation");
        }
        if (updatedUser.getEmail() != null
                && !updatedUser.getEmail().equals(currentUser.getEmail())
                && isEmailExist(updatedUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        currentUser.setName(updatedUser.getName());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        if (isAdmin) {
            currentUser.setEmail(updatedUser.getEmail());
        }
        return this.userRepository.save(currentUser);
    }

    public User findByEmail(String username) {
        return this.userRepository.findByEmail(username);

    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public List<User> handleGetAllUsers(Pageable pageable) {
        return this.userRepository.findAll(pageable).getContent();
    }

}
