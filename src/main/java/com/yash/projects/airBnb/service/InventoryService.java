package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.*;
import com.yash.projects.airBnb.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    Page<HotelPriceDTO> searchHotels(HotelSearchRequestDTO hotelSearchRequest);

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDto);
}