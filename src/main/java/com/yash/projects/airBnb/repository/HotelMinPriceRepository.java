package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.dto.HotelPriceDTO;
import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {
    @Query("""
                SELECT new com.yash.projects.airBnb.dto.HotelPriceDTO(i.hotel,AVG(i.price))
                from HotelMinPrice i
                where i.hotel.city = :city
                    and i.date  BETWEEN :startDate AND :endDate
                    and i.hotel.active = true
                GROUP BY i.hotel

""")
    Page<HotelPriceDTO> findHotelByAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
