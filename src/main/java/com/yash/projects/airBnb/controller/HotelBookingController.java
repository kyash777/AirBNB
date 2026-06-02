package com.yash.projects.airBnb.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.yash.projects.airBnb.dto.BookingDTO;
import com.yash.projects.airBnb.dto.BookingRequestDTO;
import com.yash.projects.airBnb.dto.GuestDTO;
import com.yash.projects.airBnb.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@SecurityRequirement(name = "bearerAuth")

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

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable int bookingId) {
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable int bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/status")
    public ResponseEntity<String> getBookingStatus(@PathVariable int bookingId) {
        return ResponseEntity.ok(bookingService.getBookingStatus(bookingId));
    }
}
