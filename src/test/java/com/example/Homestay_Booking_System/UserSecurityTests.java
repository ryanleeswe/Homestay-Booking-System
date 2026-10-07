package com.example.Homestay_Booking_System;

import java.util.List;
import java.util.Optional;
import com.example.Homestay_Booking_System.domain.Role;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;
import com.example.Homestay_Booking_System.service.UserService;
import com.example.Homestay_Booking_System.util.error.GlobalException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSecurityTests {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserService service = new UserService(repository, encoder);

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    private void login(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private User existingUser() {
        User user = new User(); user.setId(1L); user.setEmail("owner@example.com");
        user.setPassword(encoder.encode("old-password")); user.setRole(Role.USER);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return user;
    }

    @Test
    void registrationHashesPasswordAndRejectsRequestedAdminRole() {
        User user = new User(); user.setEmail("new@example.com"); user.setPassword("secret"); user.setRole(Role.ADMIN);
        service.handleCreateUser(user);
        assertEquals(Role.USER, user.getRole());
        assertTrue(encoder.matches("secret", user.getPassword()));
        verify(repository).save(user);
    }

    @Test
    void duplicateEmailIsConflictWithSharedFormat() {
        User user = new User(); user.setEmail("taken@example.com");
        when(repository.existsByEmail(user.getEmail())).thenReturn(true);
        var exception = assertThrows(ResponseStatusException.class, () -> service.handleCreateUser(user));
        var response = new GlobalException().handleException(exception);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict", response.getBody().getError());
        assertNull(response.getBody().getData());
        verify(repository, never()).save(any());
    }

    @Test
    void otherUserCannotReadUpdateOrDelete() {
        User user = existingUser(); login("other@example.com", "USER");
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.handleGetUserById(1)).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.handleUpdateUser(user)).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.handleDeleteUser(1)).getStatusCode().value());
        verify(repository, never()).save(any()); verify(repository, never()).delete(any(User.class));
    }

    @Test
    void ownerCanReadUpdatePasswordAndDeleteButCannotChangeEmail() {
        User current = existingUser(); login(current.getEmail(), "USER");
        assertSame(current, service.handleGetUserById(1));
        User update = new User(); update.setId(1L); update.setEmail(current.getEmail()); update.setPassword("new-password");
        service.handleUpdateUser(update);
        assertTrue(encoder.matches("new-password", current.getPassword()));
        update.setEmail("changed@example.com");
        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.handleUpdateUser(update)).getStatusCode().value());
        service.handleDeleteUser(1); verify(repository).delete(current);
    }

    @Test
    void adminCanManageOthersAndChangeEmailButNotToDuplicateEmail() {
        User current = existingUser(); login("admin@example.com", "ADMIN");
        assertSame(current, service.handleGetUserById(1));
        User update = new User(); update.setId(1L); update.setEmail("changed@example.com");
        when(repository.existsByEmail(update.getEmail())).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> service.handleUpdateUser(update)).getStatusCode().value());
        when(repository.existsByEmail(update.getEmail())).thenReturn(false);
        String originalHash = current.getPassword(); service.handleUpdateUser(update);
        assertEquals(update.getEmail(), current.getEmail()); assertEquals(originalHash, current.getPassword());
        service.handleDeleteUser(1); verify(repository).delete(current);
    }

    @Test
    void absentAuthenticationBecomes401InsteadOfNullPointerException() {
        existingUser(); SecurityContextHolder.clearContext();
        var exception = assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> service.handleGetUserById(1));
        assertEquals(401, new GlobalException().handleAuthenticationException(exception).getStatusCode().value());
    }

    @Test
    void nonexistentUserKeeps404WithSharedFormat() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class, () -> service.handleGetUserById(99));
        var response = new GlobalException().handleException(exception);
        assertEquals(404, response.getBody().getStatusCode());
        assertEquals("Not Found", response.getBody().getError()); assertNull(response.getBody().getData());
    }
}
