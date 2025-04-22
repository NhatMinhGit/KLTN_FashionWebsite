package org.example.fashion_web.backend.configurations;

import org.example.fashion_web.backend.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DefaultDiscountConfig {

    @Autowired
    private DiscountService discountService;

    @Scheduled(cron = "0 0 2 * * MON") // Mỗi thứ 2 lúc 2:00 AM
    public void applyWeeklyDiscountForLowSellingCategories() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30); // Lấy 30 ngày gần nhất
        LocalDate endDate = today;

        discountService.applyDiscountFor3LowSellingCategories(startDate, endDate);
    }
}
