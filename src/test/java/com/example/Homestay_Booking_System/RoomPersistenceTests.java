package com.example.Homestay_Booking_System;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.Homestay_Booking_System.domain.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomPersistenceTests {
    @Test
    void persistsRelationsAndEnforcesBothForeignKeys() {
        try (SessionFactory factory = new Configuration().addAnnotatedClass(Room.class)
                .addAnnotatedClass(Homestay.class).addAnnotatedClass(Booking.class)
                .addAnnotatedClass(User.class).addAnnotatedClass(Review.class)
                .addAnnotatedClass(Amenity.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:room_test;MODE=MySQL")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop").buildSessionFactory()) {
            Long roomId;
            Long homestayId;
            try (var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                Homestay homestay = new Homestay();
                homestay.setName("Test homestay");
                homestay.setLocation("Test location");
                homestay.setDescription("Test description");
                homestay.setPrice(new BigDecimal("500000.00"));
                homestay.setNumberOfGuests(2);
                session.persist(homestay);
                Room room = new Room(); room.setName("101"); room.setMaxGuests(2);
                room.setPricePerNight(new BigDecimal("500000.00")); room.setHomestay(homestay);
                session.persist(room);
                for (int i = 0; i < 2; i++) {
                    User user = new User();
                    user.setName("Test user " + i);
                    user.setEmail("test" + i + "@example.com");
                    user.setPassword("password");
                    session.persist(user);

                    Booking booking = new Booking();
                    booking.setUser(user);
                    booking.setHomestay(homestay);
                    booking.setRoom(room);
                    booking.setCheckInDate(LocalDate.of(2026, 1, 10 + i));
                    booking.setCheckOutDate(LocalDate.of(2026, 1, 11 + i));
                    booking.setTotalPrice(new BigDecimal("500000.00"));
                    session.persist(booking);
                }
                transaction.commit(); roomId = room.getId(); homestayId = homestay.getId();
            }
            try (var session = factory.openSession()) {
                Room room = session.find(Room.class, roomId);
                assertEquals(homestayId, room.getHomestay().getId());
                assertEquals(2, room.getBookings().size());
                assertEquals(roomId, room.getBookings().get(0).getRoom().getId());
                assertEquals(1, session.find(Homestay.class, homestayId).getRooms().size());
                assertEquals(new BigDecimal("500000.00"), room.getPricePerNight());
            }
            assertInvalid(factory, "insert into bookings (room_id) values (99999)");
            assertInvalid(factory, "insert into rooms (name, capacity, price_per_night, homestay_id) values ('bad', 1, 1, 99999)");
        }
    }

    private void assertInvalid(SessionFactory factory, String sql) {
        try (var session = factory.openSession()) {
            var transaction = session.beginTransaction();
            try {
                assertThrows(ConstraintViolationException.class, () -> session.createNativeMutationQuery(sql).executeUpdate());
            } finally {
                transaction.rollback();
            }
        }
    }
}
