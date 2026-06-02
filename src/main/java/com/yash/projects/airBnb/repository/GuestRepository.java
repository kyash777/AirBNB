package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.entity.Guest;
import com.yash.projects.airBnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}