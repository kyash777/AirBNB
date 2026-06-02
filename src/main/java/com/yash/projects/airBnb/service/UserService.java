package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.dto.ProfileUpdateRequestDTO;
import com.yash.projects.airBnb.dto.UserDTO;
import com.yash.projects.airBnb.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User getUserById(Long userId);

    void updateProfile(ProfileUpdateRequestDTO profileUpdateRequestDto);

    UserDTO getMyProfile();
}
