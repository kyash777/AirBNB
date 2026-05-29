package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.BookingDTO;
import com.yash.projects.airBnb.dto.BookingRequestDTO;
import com.yash.projects.airBnb.dto.GuestDTO;
import com.yash.projects.airBnb.entity.*;
import com.yash.projects.airBnb.entity.enums.BookingStatus;
import com.yash.projects.airBnb.exception.ResourceNotFoundException;
import com.yash.projects.airBnb.exception.UnAuthorizedException;
import com.yash.projects.airBnb.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor

public class BookingServiceImplementation implements BookingService {
    private final GuestRepository guestRepository;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public BookingDTO initializeBooking(BookingRequestDTO bookingRequestDTO) {

        Hotel hotel = hotelRepository.findById(bookingRequestDTO.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + bookingRequestDTO.getHotelId()));

        Room room = roomRepository.findById(bookingRequestDTO.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + bookingRequestDTO.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),
                bookingRequestDTO.getCheckInDate(),
                bookingRequestDTO.getCheckOutDate(),
                bookingRequestDTO.getRoomsCount()
        );

        long daysCount = ChronoUnit.DAYS.between(bookingRequestDTO.getCheckInDate(), bookingRequestDTO.getCheckOutDate());

        if(inventoryList.size() <daysCount){
            throw new ResourceNotFoundException("Room is not available anymore");
        }

        for(Inventory inventory : inventoryList){
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequestDTO.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDTO.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    public BookingDTO addGuests(Integer bookindId, List<GuestDTO> guestDTOList) {

        Booking booking = bookingRepository.findById(bookindId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookindId));
        User user = getCurrentUser();

        if(user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does nor belong to this user with id:"+ user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking booking has expired");
        }
        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking booking status is not RESERVED");
        }

        for(GuestDTO guestDTO : guestDTOList){
            Guest guest = modelMapper.map(guestDTO, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking =  bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDTO.class);
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
