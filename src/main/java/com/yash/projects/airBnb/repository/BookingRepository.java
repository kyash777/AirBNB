package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.entity.Booking;
import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    Collection<Object> findByUser(User user);
}
