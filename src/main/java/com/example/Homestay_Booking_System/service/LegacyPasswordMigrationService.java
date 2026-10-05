package com.example.Homestay_Booking_System.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Homestay_Booking_System.domain.User;
import com.example.Homestay_Booking_System.repository.UserRepository;

@Service
public class LegacyPasswordMigrationService {
    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LegacyPasswordMigrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public int encodeLegacyPasswords() {
        List<User> users = userRepository.findAll();
        int migratedCount = 0;

        for (User user : users) {
            String password = user.getPassword();
            if (password != null && !BCRYPT_HASH.matcher(password).matches()) {
                user.setPassword(passwordEncoder.encode(password));
                migratedCount++;
            }
        }

        if (migratedCount > 0) {
            userRepository.saveAll(users);
        }
        return migratedCount;
    }
}
