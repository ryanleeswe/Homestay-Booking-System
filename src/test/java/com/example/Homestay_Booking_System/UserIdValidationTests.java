package com.example.Homestay_Booking_System;

import java.util.Optional;
import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;
import com.example.Homestay_Booking_System.service.UserService;
import com.example.Homestay_Booking_System.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserIdValidationTests {
    @Test
    void invalidIdsDoNotReachPersistence() {
        UserRepository repository = mock(UserRepository.class);
        UserService service = new UserService(repository, mock(PasswordEncoder.class));
        assertThrows(IdInvalidException.class, () -> service.handleGetUserById(0));
        assertThrows(IdInvalidException.class, () -> service.handleDeleteUser(-1));
        assertThrows(IdInvalidException.class, () -> service.handleUpdateUser(new User()));
        verifyNoInteractions(repository);
    }

    @Test
    void missingUserIsAnErrorInsteadOfNullSuccess() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());
        UserService service = new UserService(repository, mock(PasswordEncoder.class));
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.handleGetUserById(99)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.handleDeleteUser(99)).getStatusCode().value());
        verify(repository, never()).deleteById(anyLong());
    }
}
