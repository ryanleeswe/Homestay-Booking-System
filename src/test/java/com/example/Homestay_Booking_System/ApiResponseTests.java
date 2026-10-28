package com.example.Homestay_Booking_System;

import java.util.List;
import java.util.Map;
import com.example.Homestay_Booking_System.config.SecurityConfig;
import com.example.Homestay_Booking_System.controller.AuthController;
import com.example.Homestay_Booking_System.controller.UserController;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.dto.response.RestResponse;
import com.example.Homestay_Booking_System.service.AuthService;
import com.example.Homestay_Booking_System.service.UserService;
import com.example.Homestay_Booking_System.util.ResponseBodyAdvice;
import com.example.Homestay_Booking_System.util.error.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig(ApiResponseTests.Config.class)
class ApiResponseTests {
    @Autowired WebApplicationContext context;
    @Autowired AuthService auth;
    @Autowired UserService users;
    @Autowired JwtDecoder decoder;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(auth, users, decoder);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void loginWrapsTokenPair() throws Exception {
        when(auth.login("a@example.com", "secret")).thenReturn(new AuthService.TokenPair("access", "refresh", 900, 604800));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.data.accessToken").value("access"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void loginAndRefreshFailuresUseGlobalHandler() throws Exception {
        when(auth.login(anyString(), anyString())).thenThrow(new BadCredentialsException("internal details"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@example.com\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.data").value(nullValue()));
        when(auth.refresh("bad")).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"bad\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void missingAndInvalidJwtReturnJson() throws Exception {
        mvc.perform(get("/users")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.statusCode").value(401)).andExpect(jsonPath("$.data").value(nullValue()));
        when(decoder.decode("bad")).thenThrow(new BadJwtException("Invalid token"));
        mvc.perform(get("/users").header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void adminRulesArePreserved() throws Exception {
        mvc.perform(get("/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền thực hiện thao tác này"))
                .andExpect(jsonPath("$.data").value(nullValue()));
        mvc.perform(get("/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.message").value("Admin access granted"));
    }

    @Test
    void createdUserPreservesStatusAndHidesPassword() throws Exception {
        User user = new User(); user.setId(1L); user.setEmail("a@example.com"); user.setPassword("secret");
        when(users.handleCreateUser(any())).thenReturn(user);
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Create a new user"))
                .andExpect(jsonPath("$.data.id").value(1)).andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void validationAndMalformedJsonAre400() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("Email is required")))
                .andExpect(jsonPath("$.message", containsString("Password is required")))
                .andExpect(jsonPath("$.data").value(nullValue()));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void listAndEmptySuccessWork() throws Exception {
        when(users.handleGetAllUsers(any())).thenReturn(List.of());
        mvc.perform(get("/users").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        mvc.perform(delete("/users/1").with(jwt())).andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200)).andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void invalidPaginationAndIdAre400() throws Exception {
        mvc.perform(get("/users?current=0").with(jwt())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
        mvc.perform(get("/users?current=abc").with(jwt())).andExpect(status().isBadRequest());
        when(users.handleGetUserById(99)).thenThrow(new IdInvalidException("Invalid ID"));
        mvc.perform(get("/users/99").with(jwt())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid ID"));
    }

    @Test
    void missingResourceAndUnsupportedMethodKeepStatus() throws Exception {
        mvc.perform(get("/test/missing").with(jwt())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
        mvc.perform(post("/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.statusCode").value(405));
    }

    @Test
    void unexpectedErrorDoesNotExposeDetails() throws Exception {
        mvc.perform(get("/test/crash").with(jwt())).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500)).andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống"));
    }

    @Test
    void stringAlreadyWrappedAndNoContent() throws Exception {
        mvc.perform(get("/test/text").with(jwt())).andExpect(jsonPath("$.data").value("hello"));
        mvc.perform(get("/test/wrapped").with(jwt())).andExpect(jsonPath("$.data").value("hello"));
        mvc.perform(get("/test/empty").with(jwt())).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/test/raw-error").with(jwt())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("untouched")).andExpect(jsonPath("$.data").doesNotExist());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AuthController.class, UserController.class, GlobalException.class,
            SecurityExceptionHandler.class, ResponseBodyAdvice.class, TestController.class})
    static class Config {
        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean UserService userService() { return mock(UserService.class); }
        @Bean JwtDecoder jwtDecoder() { return mock(JwtDecoder.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/text") String text() { return "hello"; }
        @GetMapping("/test/wrapped") RestResponse<String> wrapped() { return new RestResponse<>(200, "OK", null, "hello"); }
        @GetMapping("/test/empty") ResponseEntity<Void> empty() { return ResponseEntity.noContent().build(); }
        @GetMapping("/test/crash") void crash() { throw new IllegalStateException("database secret"); }
        @GetMapping("/test/raw-error") ResponseEntity<?> rawError() { return ResponseEntity.badRequest().body(Map.of("reason", "untouched")); }
        @GetMapping("/test/missing") void missing() throws NoResourceFoundException {
            throw new NoResourceFoundException(HttpMethod.GET, "missing", "missing");
        }
    }
}
