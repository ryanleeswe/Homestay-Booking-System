package com.example.Homestay_Booking_System.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.service.AuthService;
import com.example.Homestay_Booking_System.service.AuthService.TokenPair;
import com.example.Homestay_Booking_System.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public Map<String, String> health() {
        return Map.of("message", "Homestay Booking System API is running");
    }

    @PostMapping("/auth/login")
    @ApiMessage("Đăng nhập thành công")
    public TokenPair login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/auth/refresh")
    @ApiMessage("Làm mới token thành công")
    public TokenPair refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Refresh token revoked"));
    }

    @GetMapping("/admin")
    public Map<String, String> admin() {
        return Map.of("message", "Admin access granted");
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}
}
