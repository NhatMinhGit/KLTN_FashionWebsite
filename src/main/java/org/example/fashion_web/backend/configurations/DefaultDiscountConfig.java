package org.example.fashion_web.backend.configurations;

import org.example.fashion_web.backend.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

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
    @Scheduled(cron = "0 0 3 * * *") // Chạy hàng ngày lúc 3:00 sáng
    public void applyHolidayDiscounts() {
        LocalDate today = LocalDate.now();
        MonthDay current = MonthDay.from(today);

        Map<MonthDay, String> holidayMap = new HashMap<>();
        holidayMap.put(MonthDay.of(1, 1), "Ưu đãi Tết Dương Lịch");    // Tết Dương lịch
        holidayMap.put(MonthDay.of(4, 30), "Khuyến mãi Giải phóng Miền Nam"); // Giải phóng Miền Nam
        holidayMap.put(MonthDay.of(5, 1), "Ưu đãi Quốc tế Lao động"); // Quốc tế Lao động
        holidayMap.put(MonthDay.of(9, 2), "Khuyến mãi Quốc Khánh");   // Quốc khánh
        holidayMap.put(MonthDay.of(12, 25), "Giảm giá Giáng Sinh");   // Giáng sinh
        holidayMap.put(MonthDay.of(10, 10), "Khuyến mãi Ngày Phụ nữ Việt Nam"); // Ngày Phụ nữ Việt Nam
        holidayMap.put(MonthDay.of(3, 8), "Ưu đãi Quốc tế Phụ nữ"); // Quốc tế Phụ nữ
        holidayMap.put(MonthDay.of(12, 31), "Giảm giá Chào năm mới"); // Chào năm mới
        holidayMap.put(MonthDay.of(7, 4), "Ưu đãi Ngày Độc lập Mỹ"); // Ngày Độc lập Mỹ
        holidayMap.put(MonthDay.of(2, 14), "Khuyến mãi Lễ tình nhân"); // Lễ tình nhân
        holidayMap.put(MonthDay.of(11, 1), "Ưu đãi Ngày Lễ Tạ Ơn"); // Ngày Lễ Tạ Ơn
        holidayMap.put(MonthDay.of(6, 21), "Khuyến mãi Ngày của Cha"); // Ngày của Cha
        holidayMap.put(MonthDay.of(5, 9), "Giảm giá Ngày Chiến thắng"); // Ngày Chiến thắng
        holidayMap.put(MonthDay.of(11, 11), "Khuyến mãi Ngày Độc thân"); // Ngày Độc thân
        holidayMap.put(MonthDay.of(8, 15), "Ưu đãi Ngày Lễ Quốc tế Giải phóng"); // Ngày lễ Quốc tế Giải phóng

        // Kiểm tra nếu ngày hôm nay là ngày lễ trong danh sách
        if (holidayMap.containsKey(current)) {
            String promoName = holidayMap.get(current);
            discountService.applyHolidayDiscount(today, promoName);
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
