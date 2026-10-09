package com.example.Homestay_Booking_System.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Homestay Booking System API",
        version = "1.0",
        description = "API quản lý người dùng, homestay, phòng và đặt phòng"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Dán access token vào đây; không thêm chữ Bearer ở đầu")
public class OpenApiConfig {
}
