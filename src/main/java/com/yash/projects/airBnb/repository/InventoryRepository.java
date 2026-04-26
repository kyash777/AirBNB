package com.yash.projects.airBnb.repository;

import com.yash.projects.airBnb.entity.Inventory;
import com.yash.projects.airBnb.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    void deleteByDateAfterAndRoom(LocalDate date, Room room);
}
