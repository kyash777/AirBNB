package com.yash.projects.airBnb.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.yash.projects.airBnb.dto.BookingDTO;
import com.yash.projects.airBnb.dto.BookingRequestDTO;
import com.yash.projects.airBnb.dto.GuestDTO;
import com.yash.projects.airBnb.entity.*;
import com.yash.projects.airBnb.entity.enums.BookingStatus;
import com.yash.projects.airBnb.exception.ResourceNotFoundException;
import com.yash.projects.airBnb.exception.UnAuthorizedException;
import com.yash.projects.airBnb.repository.*;
import com.yash.projects.airBnb.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend-url}")
    private String frontendUrl;

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

        if(inventoryList.size() != daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }

        inventoryRepository.initBooking(room.getId(),bookingRequestDTO.getCheckInDate(),bookingRequestDTO.getCheckOutDate(),bookingRequestDTO.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDTO.getRoomsCount()));


        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDTO.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    public BookingDTO addGuests(Integer bookindId, List<GuestDTO> guestDTOList) {

        Booking booking = bookingRepository.findById(bookindId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookindId));
        User user = getCurrentUser();

        // Ensure the booking belongs to the current user. The original check was inverted
        // (it threw when the users matched). Fixing to throw when they DO NOT match.
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: " + user.getId());
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

    @Override
    @Transactional
    public String initiatePayments(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void cancelBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        // handle the refund

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        return booking.getBookingStatus().name();
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
