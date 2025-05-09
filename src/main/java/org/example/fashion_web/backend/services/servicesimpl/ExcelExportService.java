package org.example.fashion_web.backend.services.servicesimpl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.OrderStatusDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public ByteArrayInputStream categoriesToExcel(List<CategoryRevenueDto> categories, LocalDate startDate, LocalDate endDate) throws IOException {
        String[] columns = {"Danh mục", "Doanh thu"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Top Categories");

            // Title row
            String title = "Top danh mục doanh thu từ " + startDate.format(formatter) + " đến " + endDate.format(formatter);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));

            // Header row
            Row headerRow = sheet.createRow(1);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 2;
            for (CategoryRevenueDto dto : categories) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getName());
                row.createCell(1).setCellValue(dto.getSales().doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    public ByteArrayInputStream orderStatusToExcel(List<OrderStatusDto> data, LocalDate start, LocalDate end) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Order Status");

            // Tạo tiêu đề
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Trạng thái thanh toán");
            headerRow.createCell(1).setCellValue("Số lượng");

            // Ghi dữ liệu
            int rowIdx = 1;
            for (OrderStatusDto dto : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getStatus());
                row.createCell(1).setCellValue(dto.getCount());
            }

            // Auto size cột
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Xuất ra ByteArrayInputStream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public ByteArrayInputStream productsToExcel(List<ProductRevenueDto> products, LocalDate startDate, LocalDate endDate) throws IOException {
        String[] columns = {"Sản phẩm", "Doanh thu"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Top Products");

            // Title row
            String title = "Top sản phẩm doanh thu từ " + startDate.format(formatter) + " đến " + endDate.format(formatter);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));

            // Header row
            Row headerRow = sheet.createRow(1);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 2;
            for (ProductRevenueDto dto : products) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getName());
                row.createCell(1).setCellValue(dto.getSales().doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }


    public ByteArrayInputStream revenueByYearToExcel(List<String> labels, List<BigDecimal> data, String title) throws IOException {
        String[] columns = {"Năm", "Doanh thu"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Revenue by Year");

            // Style cho tiêu đề
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style cho dữ liệu
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);

            // Viết tiêu đề
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            // Merge ô tiêu đề
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columns.length - 1));

            // Viết header
            Row headerRow = sheet.createRow(1);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 2;
            if (labels.isEmpty()) {
                Row row = sheet.createRow(rowIdx);
                Cell cell = row.createCell(0);
                cell.setCellValue("Không có dữ liệu");
                cell.setCellStyle(dataStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, columns.length - 1));
            } else {
                for (int i = 0; i < labels.size(); i++) {
                    Row row = sheet.createRow(rowIdx++);
                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(labels.get(i));
                    cell0.setCellStyle(dataStyle);

                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(data.get(i).doubleValue());
                    cell1.setCellStyle(dataStyle);
                }
            }

            // Auto size cột
            for (int col = 0; col < columns.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }


}
