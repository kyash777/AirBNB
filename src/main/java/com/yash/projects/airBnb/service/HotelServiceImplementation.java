package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.Room;
import com.yash.projects.airBnb.exception.ResourceNotFoundException;
import com.yash.projects.airBnb.repository.HotelRepository;
import com.yash.projects.airBnb.repository.InventoryRepository;
import com.yash.projects.airBnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImplementation implements HotelService {
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public HotelDTO createNewHotel(HotelDTO hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        hotel=hotelRepository.save(hotel);
        log.info("Hotel created with ID: {}", hotel.getId());
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public HotelDTO getHotelById(Long id) {
        log.info("Getting hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public HotelDTO updateHotelById(Long id, HotelDTO hotel) {
        log.info("Updating hotel with ID: {}", id);
        Hotel existingHotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));

        modelMapper.map(hotel,existingHotel);
        existingHotel.setId(id);
        existingHotel=hotelRepository.save(existingHotel);
        return modelMapper.map(existingHotel,HotelDTO.class);
    }

    @Override
    @Transactional
    public Boolean deleteHotelById(Long id) {
        boolean exists = hotelRepository.existsById(id);
        if(!exists){
            throw new ResourceNotFoundException("Hotel not found with ID: " + id);
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));

        hotelRepository.deleteById(id);

        for(Room rooms : hotel.getRooms() ){
            inventoryService.deleteFutureInventories(rooms);
        }

        return exists;
    }

    @Override
    public void activateHotelById(Long id) {
        log.info("Activating hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));
        hotel.setActive(true);

        //assuming only do it once
        for(Room rooms : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(rooms);
        }
        hotelRepository.save(hotel);
    }


}
