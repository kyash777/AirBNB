package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.dto.HotelInfoDto;
import com.yash.projects.airBnb.entity.Hotel;

import java.util.List;

public interface HotelService {
    HotelDTO createNewHotel(HotelDTO hotel);
    HotelDTO getHotelById(Long id);
    HotelDTO updateHotelById(Long id, HotelDTO hotel);
    Boolean deleteHotelById(Long id);
    void activateHotelById(Long id);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDTO> getAllHotels();
}
