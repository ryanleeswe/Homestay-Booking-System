package com.example.Homestay_Booking_System.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.Homestay_Booking_System.service.AuthService;
import com.example.Homestay_Booking_System.service.AuthService.TokenPair;
import com.example.Homestay_Booking_System.util.annotation.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@Tag(name = "Authentication", description = "Đăng nhập, làm mới token và đăng xuất")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    @Operation(summary = "Kiểm tra API có đang chạy")
    public Map<String, String> health() {
        return Map.of("message", "Homestay Booking System API is running");
    }

    @PostMapping("/auth/login")
    @ApiMessage("Đăng nhập thành công")
    @Operation(summary = "Đăng nhập và nhận access token cùng refresh token")
    public TokenPair login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/auth/refresh")
    @ApiMessage("Làm mới token thành công")
    @Operation(summary = "Dùng refresh token để cấp cặp token mới")
    public TokenPair refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/auth/logout")
    @ApiMessage("Đăng xuất thành công")
    @Operation(summary = "Đăng xuất", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Logged out; refresh tokens revoked"));
    }

    @GetMapping("/admin")
    @Operation(summary = "Kiểm tra quyền admin", security = @SecurityRequirement(name = "bearerAuth"))
    public Map<String, String> admin() {
        return Map.of("message", "Admin access granted");
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}
}
