package org.example.fashion_web.backend.configurations;

import org.example.fashion_web.backend.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.List;

@Component
public class DefaultDiscountConfig {

    @Autowired
    private DiscountService discountService;

    // Giảm giá hàng tuần cho sản phẩm bán chậm
    @Scheduled(cron = "0 0 2 * * MON") // Mỗi thứ 2 lúc 2:00 AM
    public void applyWeeklyDiscountForLowSellingCategories() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30); // 30 ngày gần nhất
        LocalDate endDate = today;

        discountService.applyDiscountFor3LowSellingCategories(startDate, endDate);
        discountService.applyDiscountFor10LowSellingProducts(startDate, endDate);
    }

    // Giảm giá vào các ngày lễ cố định (ví dụ: 01/01, 02/09, 25/12, ...)
    @Scheduled(cron = "0 0 3 * * *") // Kiểm tra mỗi ngày lúc 3:00 AM
    public void applyHolidayDiscounts() {
        LocalDate today = LocalDate.now();
        MonthDay current = MonthDay.from(today);

        List<MonthDay> holidays = Arrays.asList(
                MonthDay.of(1, 1),    // Tết Dương lịch
                MonthDay.of(4, 30),   // Giải phóng miền Nam
                MonthDay.of(5, 1),    // Quốc tế lao động
                MonthDay.of(9, 2),    // Quốc khánh
                MonthDay.of(12, 25)   // Giáng sinh
        );

        if (holidays.contains(current)) {
            discountService.applyHolidayDiscount(today);
        }
    }

    // Giảm giá vào các ngày có ngày == tháng (VD: 01/01, 02/02, ..., 12/12)
    @Scheduled(cron = "0 0 4 * * *") // Kiểm tra mỗi ngày lúc 4:00 AM
    public void applySameDayMonthDiscount() {
        LocalDate today = LocalDate.now();

        if (today.getDayOfMonth() == today.getMonthValue()) {
            discountService.applySpecialDayDiscount(today);
        }
    }
}
