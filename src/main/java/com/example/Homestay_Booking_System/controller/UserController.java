package com.example.Homestay_Booking_System.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.service.UserService;
import com.example.Homestay_Booking_System.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<User> createNewUser(@Valid @RequestBody User postManUser) {

        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());

        User createdUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.handleCreateUser(createdUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user by ID")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {

        User currentUser = this.userService.handleGetUserById(id);

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id) {
        User getUser = this.userService.handleGetUserById(id);

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetUserById(id));
    }

    @PutMapping("/users")
    @ApiMessage("Update an existing user")
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {
        User resultUser = this.userService.handleUpdateUser(updatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleUpdateUser(resultUser));
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
