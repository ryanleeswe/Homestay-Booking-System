package com.example.Homestay_Booking_System.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class LegacyPasswordMigrationRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(LegacyPasswordMigrationRunner.class);

    private final LegacyPasswordMigrationService migrationService;

    public LegacyPasswordMigrationRunner(LegacyPasswordMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int migratedCount = migrationService.encodeLegacyPasswords();
        if (migratedCount > 0) {
            logger.info("Encoded {} legacy user password(s) with BCrypt", migratedCount);
        }
    }
}
