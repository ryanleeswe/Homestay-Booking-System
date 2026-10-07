package com.example.Homestay_Booking_System.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.example.Homestay_Booking_System.domain.Homestay;
import com.example.Homestay_Booking_System.domain.Room;
import com.example.Homestay_Booking_System.dto.request.RoomRequest;
import com.example.Homestay_Booking_System.dto.response.RoomResponse;
import com.example.Homestay_Booking_System.repository.HomestayRepository;
import com.example.Homestay_Booking_System.repository.RoomRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/homestays/{homestayId}/rooms")
public class RoomController {
    private final RoomRepository rooms;
    private final HomestayRepository homestays;

    public RoomController(RoomRepository rooms, HomestayRepository homestays) {
        this.rooms = rooms;
        this.homestays = homestays;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@PathVariable Long homestayId, @Valid @RequestBody RoomRequest request) {
        Homestay homestay = homestays.findById(homestayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Homestay not found"));
        Room room = new Room();
        room.setHomestay(homestay);
        room.setName(request.name());
        room.setMaxGuests(request.maxGuests());
        room.setPricePerNight(request.pricePerNight());
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.from(rooms.save(room)));
    }

    @GetMapping
    public List<RoomResponse> list(@PathVariable Long homestayId) {
        if (!homestays.existsById(homestayId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Homestay not found");
        return rooms.findByHomestayId(homestayId).stream().map(RoomResponse::from).toList();
    }
}
