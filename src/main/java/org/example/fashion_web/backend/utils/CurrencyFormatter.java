package org.example.fashion_web.backend.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component
public class CurrencyFormatter {
    public static String formatVND(BigDecimal amount) {
        if (amount == null) return "0 đ";
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " đ";
    }
}
