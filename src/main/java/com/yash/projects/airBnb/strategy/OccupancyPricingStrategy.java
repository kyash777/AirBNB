package com.yash.projects.airBnb.strategy;

import com.yash.projects.airBnb.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        BigDecimal price = wrapped.calculatePrice(inventory);
        double occupancyRate = (double) inventory.getBookedCount()/ inventory.getTotalCount();

        if(occupancyRate > 0.8){
            price = price.multiply(BigDecimal.valueOf(1.2)); // Increase price by 20% if occupancy is above 80%
        }

        return price;
    }
}
