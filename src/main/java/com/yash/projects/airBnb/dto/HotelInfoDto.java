package com.yash.projects.airBnb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfoDto {
    private HotelDTO hotelDTO;
    private List<RoomDTO> rooms;

}
