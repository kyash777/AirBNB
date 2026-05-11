package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.dto.HotelSearchRequestDTO;
import com.yash.projects.airBnb.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    Page<HotelDTO> searchHotels(HotelSearchRequestDTO hotelSearchRequest);

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

}