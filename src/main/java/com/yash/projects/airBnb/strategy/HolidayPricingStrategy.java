package com.yash.projects.airBnb.strategy;

import com.yash.projects.airBnb.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isHolidayToday = true;

        if(isHolidayToday){
            price = price.multiply(BigDecimal.valueOf(1.25));
        }

        return price;
    }
}
