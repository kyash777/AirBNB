package com.yash.projects.airBnb.dto;

import com.yash.projects.airBnb.entity.Guest;
import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.Room;
import com.yash.projects.airBnb.entity.User;
import com.yash.projects.airBnb.entity.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDTO {
    private Long id;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDTO> guests;
    private BigDecimal amount;
}
