package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}
