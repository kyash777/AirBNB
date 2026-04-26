package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.HotelDTO;
import com.yash.projects.airBnb.entity.Hotel;

public interface HotelService {
    HotelDTO createNewHotel(HotelDTO hotel);
    HotelDTO getHotelById(Long id);
    HotelDTO updateHotelById(Long id, HotelDTO hotel);
    Boolean deleteHotelById(Long id);
    void activateHotelById(Long id);
}
