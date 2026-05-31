package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Optional<Booking> findByPaymentSessionId(String sessionId);
}
