package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.GuestDTO;

import java.util.List;

public interface GuestService {

    List<GuestDTO> getAllGuests();

    void updateGuest(Long guestId, GuestDTO guestDTO);

    void deleteGuest(Long guestId);

    GuestDTO addNewGuest(GuestDTO guestDTO);
}
