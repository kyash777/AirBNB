package com.yash.projects.airBnb.controller;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j

public class HotelController {
    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelDTO> addNewHotel(@RequestBody HotelDTO hotelDto){
        log.info("Adding hotel with name {}",hotelDto.getName());
        HotelDTO hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel,HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable Long hotelId){
        log.info("getting hotel with id {}",hotelId);
        HotelDTO hotelDto = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDTO> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDTO hotelDto){
        log.info("Updating hotel with id {}",hotelId);
        HotelDTO updatedHotel = hotelService.updateHotelById(hotelId,hotelDto);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        log.info("deleting hotel with id {}",hotelId);
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}")
    public ResponseEntity<Void> activateHotelById(@PathVariable Long hotelId){
        log.info("activating hotel with id {}",hotelId);
        hotelService.activateHotelById(hotelId);
        return ResponseEntity.noContent().build();

    }


}
