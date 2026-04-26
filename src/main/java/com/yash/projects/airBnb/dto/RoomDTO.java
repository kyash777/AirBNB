package com.yash.projects.airBnb.dto;

import com.yash.projects.airBnb.entity.HotelContactInfo;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class RoomDTO {

    private Long id;
    private String type;
    private BigDecimal basePrice;
    private String[] photos;
    private String[] amenities;
    private Integer totalCount;
    private Integer capacity;
    private Boolean active;

}
