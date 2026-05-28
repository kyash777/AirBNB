package com.yash.projects.airBnb.dto;

import com.yash.projects.airBnb.entity.User;
import com.yash.projects.airBnb.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDTO {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
