package com.example.Homestay_Booking_System.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.dto.request.UserCreateRequest;
import com.example.Homestay_Booking_System.dto.request.UserUpdateRequest;
import com.example.Homestay_Booking_System.dto.response.UserResponse;
import com.example.Homestay_Booking_System.service.UserService;
import jakarta.validation.Valid;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userService.createUser(user)));
    }

    @PutMapping("/users")
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        User changes = new User();
        changes.setName(request.name());
        changes.setEmail(request.email());
        changes.setPassword(request.password());
        return ResponseEntity.ok(UserResponse.from(userService.updateUser(request.id(), changes)));
    }

}
