package com.yash.projects.airBnb.service;

import com.yash.projects.airBnb.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

}