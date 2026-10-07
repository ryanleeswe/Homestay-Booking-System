package com.example.Homestay_Booking_System.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.service.UserService;
import com.example.Homestay_Booking_System.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<User> createNewUser(@Valid @RequestBody User postManUser) {
        User createdUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User user = this.userService.handleGetUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/users")
    @ApiMessage("Update an existing user")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User updatedUser) {
        User resultUser = this.userService.handleUpdateUser(updatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(resultUser);
    }

    @GetMapping("users")
    @ApiMessage("Get all users with pagination")
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(value = "current", defaultValue = "1") @Min(1) int current,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        PageRequest pageable = PageRequest.of(current - 1, pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUsers(pageable));
    }

    // @PostMapping("/auth/logout")
    // public ResponseEntity<Void> logout() {
    // String email = SecurityUtil.getCurrentUserLogin().isPresent() ?
    // SecurityUtil.getCurrentUserLogin().get() : "";

    // if (email.equals("")) {
    // throw new IdInvalidException("User is not logged in");
    // }

    // this.userService.updateUserToken(null, email);

    // ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
    // .httpOnly(true)
    // .secure(true)
    // .path("/")
    // .maxAge(0)
    // .build();
    // return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
    // deleteCookie.toString()).build();
    // }
}
