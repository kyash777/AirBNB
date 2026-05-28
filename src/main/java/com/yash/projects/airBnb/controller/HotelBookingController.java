package com.yash.projects.airBnb.controller;


import com.yash.projects.airBnb.dto.BookingDTO;
import com.yash.projects.airBnb.dto.BookingRequestDTO;
import com.yash.projects.airBnb.dto.GuestDTO;
import com.yash.projects.airBnb.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")

public class HotelBookingController {
    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDTO> initializeBookings(@RequestBody BookingRequestDTO bookingRequestDTO) {
        return ResponseEntity.ok(bookingService.initializeBooking(bookingRequestDTO));
    }

    @PostMapping("/{bookindId}/addGuests")
    public ResponseEntity<BookingDTO> addGuests(@RequestBody List<GuestDTO> guestDTOList, @PathVariable Integer bookindId){

        return ResponseEntity.ok(bookingService.addGuests(bookindId,guestDTOList));
    }
}
