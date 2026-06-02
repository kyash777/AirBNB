package com.yash.projects.airBnb.service;

import com.stripe.model.Event;
import com.yash.projects.airBnb.dto.BookingDTO;
import com.yash.projects.airBnb.dto.BookingRequestDTO;
import com.yash.projects.airBnb.dto.GuestDTO;
import com.yash.projects.airBnb.dto.HotelReportDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO);

    BookingDTO addGuests(Integer bookindId, List<GuestDTO> guestDTOList);

    String initiatePayments(int bookingId);

    void capturePayment(Event event);

    void cancelBooking(int bookingId);

    String getBookingStatus(int bookingId);

    List<BookingDTO> getAllBookingsByHotelId(Long hotelId);

    HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDTO> getMyBookings();
}
