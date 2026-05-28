package com.yash.projects.airBnb.strategy;

import com.yash.projects.airBnb.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
