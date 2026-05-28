package com.yash.projects.airBnb.controller;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.dto.HotelInfoDto;
import com.yash.projects.airBnb.dto.HotelPriceDTO;
import com.yash.projects.airBnb.dto.HotelSearchRequestDTO;
import com.yash.projects.airBnb.service.HotelService;
import com.yash.projects.airBnb.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor

public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @PostMapping("/search")
    public ResponseEntity<Page<HotelPriceDTO>> searchHotelsByRequestBody(@RequestBody HotelSearchRequestDTO hotelSearchRequest) {
        Page<HotelPriceDTO> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        HotelInfoDto hotelInfo = hotelService.getHotelInfoById(hotelId);
        return ResponseEntity.ok(hotelInfo);
    }
}
