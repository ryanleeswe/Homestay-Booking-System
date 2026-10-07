package com.example.Homestay_Booking_System.dto.response;

import java.math.BigDecimal;
import com.example.Homestay_Booking_System.domain.Room;

public record RoomResponse(Long id, Long homestayId, String name, int maxGuests, BigDecimal pricePerNight) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getHomestay().getId(), room.getName(), room.getMaxGuests(), room.getPricePerNight());
    }
}
