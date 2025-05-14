package org.example.fashion_web.backend.scheduler;

import org.example.fashion_web.backend.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiscountStatusScheduler {

    @Autowired
    private DiscountService discountService;

    @Scheduled(fixedRate = 10000) // Chạy mỗi 60 giây
    public void updateDiscountStatuses() {
        discountService.updateDiscountStatuses();
    }
}
