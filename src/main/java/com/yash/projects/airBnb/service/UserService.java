package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User getUserById(Long userId);
}
