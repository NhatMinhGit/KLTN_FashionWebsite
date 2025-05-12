package org.example.fashion_web.frontend.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.example.fashion_web.backend.dto.WarehouseForm;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class WarehouseManageController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductVariantService productVariantService;
    @Autowired
    private SizeService sizeService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionGroupService transactionGroupService;
    @Autowired
    private SpringTemplateEngine templateEngine; // Thymeleaf template engine
    @GetMapping("/admin/warehouse")
    public String listProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model, Principal principal) {

        // Xử lý thông tin người dùng nếu có
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            }
        }

        // Kiểm tra nếu có thông báo thành công
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.asMap().get("successMessage"));
        }

        // Kiểm tra tùy chọn in hóa đơn
        if (model.containsAttribute("showInvoiceOption")) {
            model.addAttribute("showInvoiceOption", model.asMap().get("showInvoiceOption"));
        }

        // Lấy tổng số sản phẩm
        int totalProducts = productService.getTotalProductsCount();

        // Lấy tất cả TransactionGroups
        List<TransactionGroup> transactionGroups = transactionGroupService.getAllTransactionGroups();
        model.addAttribute("transactionGroups", transactionGroups);

        // Nếu số sản phẩm > 10, dùng phân trang
        if (totalProducts > 10) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage = productService.getAllProducts(pageable);

            model.addAttribute("productPage", productPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "product/warehouse-management";
        }
        else {
            // Nếu số sản phẩm <= 10, trả về toàn bộ danh sách
            List<Product> products = productService.getAllProducts();
            model.addAttribute("products", products);
            return "product/warehouse-management";
        }
    }
    @GetMapping("/admin/products/announce-warehouse")
    public String announceWarehouse(Model model) {
        List<Product> products = productService.getAllProducts();
        Map<Long, List<ProductVariant>> productVariants = new HashMap<>();
        Map<Long, Map<Long, Map<String, Integer>>> productVariantSizeQuantities = new HashMap<>();

        for (Product product : products) {
            List<ProductVariant> variants = product.getVariants();
            productVariants.put(product.getId(), variants);

            Map<Long, Map<String, Integer>> variantSizeMap = new HashMap<>();
            for (ProductVariant variant : variants) {
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                Map<String, Integer> sizeQuantities = new HashMap<>();
                for (Size size : sizes) {
                    sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
                }
                variantSizeMap.put(variant.getId(), sizeQuantities);
            }
            productVariantSizeQuantities.put(product.getId(), variantSizeMap);
        }

        model.addAttribute("productVariants", productVariants);
        model.addAttribute("productVariantSizeQuantities", productVariantSizeQuantities);
        model.addAttribute("products", products);
        return "product/warehouse-report";
    }

    @GetMapping("/admin/warehouse/transaction-detail/{id}")
    public String loadTransactionGroupForm(@PathVariable("id") Long groupId, Model model) {
        // Lấy TransactionGroup theo ID
        TransactionGroup transactionGroup = transactionGroupService.getTransactionGroupById(groupId);
        model.addAttribute("transactionGroup", transactionGroup);

        // Lấy danh sách Transaction thuộc group đó
        List<Transaction> transactions = transactionService.getTransactionsByGroupId(transactionGroup);
        model.addAttribute("transactions", transactions);

        return "product/transaction-detail"; // Tên file Thymeleaf (transaction-group-detail.html)
    }
    @GetMapping("/admin/warehouse/export-invoice/{id}")
    public void exportTransactionGroupPdf(@PathVariable("id") Long groupId, HttpServletResponse response) throws Exception {
        TransactionGroup transactionGroup = transactionGroupService.getTransactionGroupById(groupId);
        if (transactionGroup == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<Transaction> transactions = transactionService.getTransactionsByGroupId(transactionGroup);

        // Thymeleaf context
        Context context = new Context();
        context.setVariable("transactionGroup", transactionGroup); // Pass the transactionGroup
        context.setVariable("transactions", transactions); // Pass the transactionGroup

        // Render HTML
        String htmlContent = templateEngine.process("pdf-template/transaction-invoice", context);

        // Xuất PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=transaction-invoice.pdf");

        try (OutputStream out = response.getOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(new File("src/main/resources/fonts/DejaVuSans.ttf"), "MyFont");
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(out);
            builder.run();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF: " + e.getMessage());
        }
    }

    @GetMapping("/admin/warehouse/import-product/{id}")
    public String showImportProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

        List<ProductVariant> productVariants = productVariantService.findAllByProductId(product.getId());
        Map<Long, Map<String, Integer>> variantSizeQuantities = new HashMap<>();
        WarehouseForm warehouseForm = new WarehouseForm();
        warehouseForm.setProductId(product.getId());

        for (ProductVariant variant : productVariants) {
            List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
            Map<String, Integer> sizeQuantities = new HashMap<>();
            for (Size size : sizes) {
                sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
            }
            variantSizeQuantities.put(variant.getId(), sizeQuantities);
        }

        model.addAttribute("product", product);
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("variantSizeQuantities", variantSizeQuantities);
        model.addAttribute("warehouseForm", warehouseForm);

        return "product/import-product";  // giao diện nhập kho
    }

    @PostMapping("/admin/warehouse/import-product/{id}")
    public String importProduct(@PathVariable("id") Long id,
                                @ModelAttribute("warehouseForm") WarehouseForm warehouseForm,
                                @RequestParam List<Long> variantIds,
                                @RequestParam Map<String, String> variantSizes,
                                @RequestParam Map<String, String> variantUnitPrices,
                                RedirectAttributes redirectAttributes,
                                Principal principal) {  // Lấy thông tin người dùng đang đăng nhập
        Logger logger = LoggerFactory.getLogger(getClass());

        try {
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Tạo transaction group mới
            TransactionGroup group = new TransactionGroup();
            group.setTransactionType(TransactionType.IMPORT);
            group.setCreatedBy(principal.getName());  // Tên người dùng hiện tại
            group.setCreatedAt(LocalDateTime.now());
            group.setNote("Nhập kho cho sản phẩm " + product.getName());
            group.setTotalAmount(BigDecimal.ZERO);  // Tổng tiền sẽ được cộng dần

            List<Transaction> transactions = new ArrayList<>();

            for (Long variantId : variantIds) {
                Map<String, Integer> sizeQuantities = new HashMap<>();

                for (String key : variantSizes.keySet()) {
                    if (key.startsWith("variantSizes[" + variantId + "]")) {
                        String size = key.split("\\[")[2].split("\\]")[0];
                        Integer quantity = Integer.parseInt(variantSizes.get(key));
                        if (quantity > 0) {
                            sizeQuantities.put(size, quantity);
                        }
                    }
                }

                for (Map.Entry<String, Integer> entry : sizeQuantities.entrySet()) {
                    String sizeName = entry.getKey();
                    Integer quantity = entry.getValue();

                    String unitPriceString = variantUnitPrices.get("variantUnitPrices[" + variantId + "]");
                    BigDecimal unitPrice = new BigDecimal(unitPriceString);

                    // Cập nhật kho
                    sizeService.updateStockQuantity(variantId, sizeName, quantity);

                    Optional<Size> sizeOptional = sizeService.findByProductVariantIdAndSizeName(variantId, sizeName);
                    if (!sizeOptional.isPresent()) {
                        throw new ResourceNotFoundException("Không tìm thấy size " + sizeName + " cho variantId " + variantId);
                    }

                    Size size = sizeOptional.get();

                    // Tạo giao dịch
                    Transaction transaction = new Transaction();
                    transaction.setVariantId(variantId);
                    transaction.setSizeId(size.getId());
                    transaction.setTransactionType(TransactionType.IMPORT);
                    transaction.setQuantity(quantity);
                    transaction.setUnitPrice(unitPrice);
                    transaction.setTransactionDate(LocalDateTime.now());
                    transaction.setNote("Nhập kho " + product.getName() + " - size " + sizeName);
                    transaction.setTransactionGroup(group);  // Liên kết group

                    // Cộng dồn tổng tiền
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    group.setTotalAmount(group.getTotalAmount().add(subtotal));

                    transactions.add(transaction);
                }
            }

            // Lưu group và các transaction (cascade)
            group.setTransactions(transactions);
            transactionGroupService.save(group);

            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công!");
            return "redirect:/admin/warehouse";
        } catch (Exception e) {
            logger.error("Lỗi khi nhập kho sản phẩm với ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Nhập kho thất bại!");
            return "redirect:/admin/warehouse/import-product/" + id;
        }
    }


    @GetMapping("/admin/warehouse/export-product/{id}")
    public String showExportProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

        List<ProductVariant> productVariants = productVariantService.findAllByProductId(product.getId());
        Map<Long, Map<String, Integer>> variantSizeQuantities = new HashMap<>();
        WarehouseForm warehouseForm = new WarehouseForm();
        warehouseForm.setProductId(product.getId());

        for (ProductVariant variant : productVariants) {
            List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
            Map<String, Integer> sizeQuantities = new HashMap<>();
            for (Size size : sizes) {
                sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
            }
            variantSizeQuantities.put(variant.getId(), sizeQuantities);
        }

        model.addAttribute("product", product);
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("variantSizeQuantities", variantSizeQuantities);
        model.addAttribute("warehouseForm", warehouseForm);

        return "product/export-product";  // giao diện nhập kho
    }

    @PostMapping("/admin/warehouse/export-product/{id}")
    public String exportProduct(@PathVariable("id") Long id,
                                @ModelAttribute("warehouseForm") WarehouseForm warehouseForm,
                                @RequestParam List<Long> variantIds,
                                @RequestParam Map<String, String> variantSizes,
                                @RequestParam Map<String, String> variantUnitPrices,
                                RedirectAttributes redirectAttributes,
                                Principal principal) {  // Lấy thông tin người dùng đang đăng nhập
        Logger logger = LoggerFactory.getLogger(getClass());

        try {
            // Lấy thông tin sản phẩm theo id
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Tạo transaction group mới
            TransactionGroup group = new TransactionGroup();
            group.setTransactionType(TransactionType.EXPORT);  // Loại giao dịch: Xuất kho
            group.setCreatedBy(principal.getName());  // Tên người dùng hiện tại
            group.setCreatedAt(LocalDateTime.now());  // Ngày giờ tạo
            group.setNote("Xuất kho cho sản phẩm " + product.getName());  // Mô tả giao dịch
            group.setTotalAmount(BigDecimal.ZERO);  // Tổng tiền sẽ được cộng dần

            List<Transaction> transactions = new ArrayList<>();

            for (Long variantId : variantIds) {
                Map<String, Integer> sizeQuantities = new HashMap<>();

                // Duyệt qua các kích thước để lấy số lượng nhập
                for (String key : variantSizes.keySet()) {
                    if (key.startsWith("variantSizes[" + variantId + "]")) {
                        String size = key.split("\\[")[2].split("\\]")[0];  // Lấy tên size
                        Integer quantity = Integer.parseInt(variantSizes.get(key));  // Lấy số lượng
                        if (quantity > 0) {
                            sizeQuantities.put(size, quantity);  // Thêm vào danh sách size và số lượng
                        }
                    }
                }

                // Xử lý từng size và variant
                for (Map.Entry<String, Integer> entry : sizeQuantities.entrySet()) {
                    String sizeName = entry.getKey();
                    Integer quantity = entry.getValue();

                    // Lấy giá nhập từ variantUnitPrices
                    String unitPriceString = variantUnitPrices.get("variantUnitPrices[" + variantId + "]");
                    BigDecimal unitPrice = new BigDecimal(unitPriceString);

                    // Cập nhật kho
                    sizeService.updateStockQuantity(variantId, sizeName, -quantity);

                    Optional<Size> sizeOptional = sizeService.findByProductVariantIdAndSizeName(variantId, sizeName);
                    if (!sizeOptional.isPresent()) {
                        throw new ResourceNotFoundException("Không tìm thấy size " + sizeName + " cho variantId " + variantId);
                    }

                    Size size = sizeOptional.get();

                    // Tạo giao dịch xuất kho
                    Transaction transaction = new Transaction();
                    transaction.setVariantId(variantId);
                    transaction.setSizeId(size.getId());
                    transaction.setTransactionType(TransactionType.EXPORT);
                    transaction.setQuantity(quantity);
                    transaction.setUnitPrice(unitPrice);
                    transaction.setTransactionDate(LocalDateTime.now());
                    transaction.setNote("Xuất kho " + product.getName() + " - size " + sizeName);
                    transaction.setTransactionGroup(group);  // Liên kết transaction với group

                    // Cộng dồn tổng tiền
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    group.setTotalAmount(group.getTotalAmount().add(subtotal));

                    transactions.add(transaction);
                }
            }

            // Lưu group và các transaction (cascade)
            group.setTransactions(transactions);  // Liên kết các transaction với group
            transactionGroupService.save(group);  // Lưu group vào database

            redirectAttributes.addFlashAttribute("successMessage", "Xuất kho thành công!");
            return "redirect:/admin/warehouse";
        } catch (Exception e) {
            logger.error("Lỗi khi xuất kho sản phẩm với ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Xuất kho thất bại!");
            return "redirect:/admin/warehouse/export-product/" + id;
        }
    }

}