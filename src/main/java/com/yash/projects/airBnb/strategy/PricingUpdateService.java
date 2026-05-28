package com.yash.projects.airBnb.strategy;

import com.yash.projects.airBnb.entity.Hotel;
import com.yash.projects.airBnb.entity.HotelMinPrice;
import com.yash.projects.airBnb.entity.Inventory;
import com.yash.projects.airBnb.repository.HotelMinPriceRepository;
import com.yash.projects.airBnb.repository.HotelRepository;
import com.yash.projects.airBnb.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;

    //scheduler to update the inventory and HotelMinPrice table every 5 seconds

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void updatePrices(){
        log.info("SCHEDULER TRIGGERED: Starting scheduled price update process at {}", java.time.LocalDateTime.now());

        try {
            log.info("Starting scheduled price update process");

            int page = 0;
            int batchSize = 10;

            while (true) {
                Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
                if (hotelPage.isEmpty()) {
                    break;
                }

                log.info("Processing page {} with {} hotels", page, hotelPage.getContent().size());

                hotelPage.getContent().forEach(hotel -> {
                    try {
                        updateHotelPrices(hotel);
                    } catch (Exception e) {
                        log.error("Error updating prices for hotel ID: {}", hotel.getId(), e);
                        // Continue with next hotel instead of stopping the entire process
                    }
                });
                page++;
            }

            log.info("Completed scheduled price update process");
        } catch (Exception e) {
            log.error("Error in scheduled price update process", e);
        }
    }

    public void updateHotelPrices(Hotel hotel){
         log.info("Updating hotel prices for hotel ID: {}",hotel.getId() );
         LocalDate startDate = LocalDate.now();
         LocalDate endDate = startDate.plusYears(1);

         List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
         log.info("Found {} inventory records for hotel ID: {}", inventoryList.size(), hotel.getId());

        updateInventoryPrices(inventoryList);

        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);


    }

    public void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        log.info("Computing min prices for hotel ID: {} with {} inventory records", hotel.getId(), inventoryList.size());

        // compute minimum price perday for a hotel
        Map<LocalDate,BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(BigDecimal.ZERO)));

        log.info("Calculated {} daily min prices for hotel ID: {}", dailyMinPrices.size(), hotel.getId());

        //Prepare HotelPrice Entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        log.info("Prepared {} HotelMinPrice entities for hotel ID: {}", hotelPrices.size(), hotel.getId());

        //save all HotelPrices entities in bulk
        if (!hotelPrices.isEmpty()) {
            hotelMinPriceRepository.saveAll(hotelPrices);
            log.info("Saved {} HotelMinPrice records for hotel ID: {}", hotelPrices.size(), hotel.getId());
        } else {
            log.warn("No HotelMinPrice entities to save for hotel ID: {}", hotel.getId());
        }
    }

    public void updateInventoryPrices(List<Inventory> inventoryList){
        inventoryList.forEach(inventory ->{
            BigDecimal dynamicPrice = pricingService.calculateDtnamicPricing(inventory);
            inventory.setPrice(dynamicPrice);

        } );
        inventoryRepository.saveAll(inventoryList);
    }
}
