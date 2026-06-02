package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.dto.HotelInfoDto;
import com.yash.projects.airBnb.dto.RoomDTO;
import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.Room;
import com.yash.projects.airBnb.entity.User;
import com.yash.projects.airBnb.exception.ResourceNotFoundException;
import com.yash.projects.airBnb.exception.UnAuthorizedException;
import com.yash.projects.airBnb.repository.HotelRepository;
import com.yash.projects.airBnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.yash.projects.airBnb.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImplementation implements HotelService {
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDTO createNewHotel(HotelDTO hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel=hotelRepository.save(hotel);
        log.info("Hotel created with ID: {}", hotel.getId());
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public HotelDTO getHotelById(Long id) {
        log.info("Getting hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("User is not owner of this hotel");
        }
        return modelMapper.map(hotel,HotelDTO.class);
    }

    @Override
    public HotelDTO updateHotelById(Long id, HotelDTO hotel) {
        log.info("Updating hotel with ID: {}", id);
        Hotel existingHotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(existingHotel.getOwner())){
            throw new UnAuthorizedException("User is not owner of this hotel");
        }

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

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("User is not owner of this hotel");
        }

        for(Room rooms : hotel.getRooms() ){
            inventoryService.deleteAllInventories(rooms);
            roomRepository.deleteById(rooms.getId());

        }

        hotelRepository.deleteById(id);

        return exists;
    }

    @Override
    public void activateHotelById(Long id) {
        log.info("Activating hotel with ID: {}", id);
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("User is not owner of this hotel");
        }
        hotel.setActive(true);

        //assuming only do it once
        for(Room rooms : hotel.getRooms()){
            inventoryService.initializeRoomForAYear(rooms);
        }
        hotelRepository.save(hotel);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        List<RoomDTO> rooms = hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room, RoomDTO.class))
                .toList();

        return HotelInfoDto.builder()
                .hotelDTO(modelMapper.map(hotel, HotelDTO.class))
                .rooms(rooms)
                .build();


    }

    @Override
    public List<HotelDTO> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with ID: {}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels
                .stream()
                .map((element) -> modelMapper.map(element, HotelDTO.class))
                .collect(Collectors.toList());
    }


}
