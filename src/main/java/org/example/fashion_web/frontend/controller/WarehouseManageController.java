package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.WarehouseForm;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductVariant;
import org.example.fashion_web.backend.models.Size;
import org.example.fashion_web.backend.models.Transaction;
import org.example.fashion_web.backend.services.ProductService;
import org.example.fashion_web.backend.services.ProductVariantService;
import org.example.fashion_web.backend.services.SizeService;
import org.example.fashion_web.backend.services.TransactionService;
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
            // Nếu số sản phẩm <= 15, trả về toàn bộ danh sách
            List<Product> products = productService.getAllProducts();
            model.addAttribute("products", products);
            return "product/warehouse-management";
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
                                @RequestParam List<Long> variantIds,  // Lấy tất cả variantIds
                                @RequestParam Map<String, String> variantSizes,  // Lấy tất cả variantSizes
                                @RequestParam Map<String, String> variantUnitPrices, // Lấy giá nhập
                                RedirectAttributes redirectAttributes) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            // Lấy thông tin sản phẩm theo id
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Xử lý nhập kho cho các variant được chọn
            for (Long variantId : variantIds) {
                Map<String, Integer> sizeQuantities = new HashMap<>();

                // Duyệt qua tất cả các tham số variantSizes[variantId][size] để lấy số lượng nhập
                for (String key : variantSizes.keySet()) {
                    if (key.startsWith("variantSizes[" + variantId + "]")) {
                        // Tách size từ khóa
                        String size = key.split("\\[")[2].split("\\]")[0];  // Lấy size từ tên key
                        Integer quantity = Integer.parseInt(variantSizes.get(key));  // Số lượng nhập
                        if (quantity > 0) {
                            sizeQuantities.put(size, quantity);  // Thêm vào bản đồ sizeQuantities
                        }
                    }
                }

                // Tiến hành nhập kho cho variantId
                for (Map.Entry<String, Integer> entry : sizeQuantities.entrySet()) {
                    String sizeName = entry.getKey();  // Lấy tên size
                    Integer quantity = entry.getValue();  // Số lượng nhập kho

                    // Lấy giá nhập từ variantUnitPrices
                    String unitPriceString = variantUnitPrices.get("variantUnitPrices[" + variantId + "]");
                    BigDecimal unitPrice = new BigDecimal(unitPriceString);  // Chuyển đổi giá nhập từ String sang BigDecimal

                    // Cập nhật số lượng tồn kho cho mỗi variantId và sizeName
                    sizeService.updateStockQuantity(variantId, sizeName, quantity);

                    // Lưu giao dịch nhập kho vào bảng transaction
                    Optional<Size> sizeOptional = sizeService.findByProductVariantIdAndSizeName(variantId, sizeName);
                    if (!sizeOptional.isPresent()) {
                        throw new ResourceNotFoundException("Không tìm thấy size " + sizeName + " cho variantId " + variantId);
                    }
                    Size size = sizeOptional.get();

                    // Lưu giao dịch vào cơ sở dữ liệu
                    Transaction transaction = new Transaction();
                    transaction.setVariantId(variantId);  // Bạn có thể tạo một đối tượng ProductVariant nếu cần
                    transaction.setSizeId(size.getId());
                    transaction.setTransactionType(Transaction.TransactionType.IMPORT);
                    transaction.setQuantity(quantity);
                    transaction.setUnitPrice(unitPrice);  // Lưu giá nhập
                    transaction.setTransactionDate(LocalDateTime.now()); // Lưu ngày giao dịch
                    transaction.setNote("Nhập kho cho sản phẩm " + product.getName() + " size " + sizeName);

                    transactionService.save(transaction);  // Lưu giao dịch vào cơ sở dữ liệu
                }
            }

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
                                @RequestParam List<Long> variantIds,  // Lấy tất cả variantIds
                                @RequestParam Map<String, String> variantSizes,  // Lấy tất cả variantSizes
                                @RequestParam Map<String, String> variantUnitPrices, // Lấy giá nhập
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            // Lấy thông tin sản phẩm theo id
            Product product = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Xử lý nhập kho cho các variant được chọn
            for (Long variantId : variantIds) {
                Map<String, Integer> sizeQuantities = new HashMap<>();

                // Duyệt qua tất cả các tham số variantSizes[variantId][size] để lấy số lượng nhập
                for (String key : variantSizes.keySet()) {
                    if (key.startsWith("variantSizes[" + variantId + "]")) {
                        // Tách size từ khóa
                        String size = key.split("\\[")[2].split("\\]")[0];  // Lấy size từ tên key
                        Integer quantity = Integer.parseInt(variantSizes.get(key));  // Số lượng nhập
                        // Chỉ thêm vào nếu số lượng lớn hơn 0
                        if (quantity > 0) {
                            sizeQuantities.put(size, quantity);  // Thêm vào bản đồ sizeQuantities
                        }
                    }
                }
                // Tạo danh sách transaction để gom vào hóa đơn
                List<Transaction> transactions = new ArrayList<>();
                // Tiến hành nhập kho cho variantId
                for (Map.Entry<String, Integer> entry : sizeQuantities.entrySet()) {
                    String sizeName = entry.getKey();  // Lấy tên size
                    Integer quantity = entry.getValue();  // Số lượng nhập kho

                    // Lấy giá nhập từ variantUnitPrices
                    String unitPriceString = variantUnitPrices.get("variantUnitPrices[" + variantId + "]");
                    BigDecimal unitPrice = new BigDecimal(unitPriceString);  // Chuyển đổi giá nhập từ String sang BigDecimal

                    // Cập nhật số lượng tồn kho cho mỗi variantId và sizeName
                    sizeService.updateStockQuantity(variantId, sizeName, -quantity);

                    // Lưu giao dịch nhập kho vào bảng transaction
                    Optional<Size> sizeOptional = sizeService.findByProductVariantIdAndSizeName(variantId, sizeName);
                    if (!sizeOptional.isPresent()) {
                        throw new ResourceNotFoundException("Không tìm thấy size " + sizeName + " cho variantId " + variantId);
                    }
                    Size size = sizeOptional.get();

                    // Lưu giao dịch vào cơ sở dữ liệu
                    Transaction transaction = new Transaction();
                    transaction.setVariantId(variantId);  // Bạn có thể tạo một đối tượng ProductVariant nếu cần
                    transaction.setSizeId(size.getId());
                    transaction.setTransactionType(Transaction.TransactionType.EXPORT);
                    transaction.setQuantity(quantity);
                    transaction.setUnitPrice(unitPrice);  // Lưu giá nhập
                    transaction.setTransactionDate(LocalDateTime.now()); // Lưu ngày giao dịch
                    transaction.setNote("Nhập kho cho sản phẩm " + product.getName() + " size " + sizeName);

                    transactionService.save(transaction);  // Lưu giao dịch vào cơ sở dữ liệu
                    transactions.add(transaction);
                }
                // Sau khi lưu thành công, tạo dữ liệu hóa đơn
                model.addAttribute("transactions", transactions);
                model.addAttribute("product", product); // nếu muốn hiển thị thêm tên sản phẩm, mã, v.v.
                model.addAttribute("totalAmount", transactions.stream()
                        .map(t -> t.getUnitPrice().multiply(BigDecimal.valueOf(t.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
            }

            redirectAttributes.addFlashAttribute("successMessage", "Nhập kho thành công!");
            redirectAttributes.addFlashAttribute("showInvoiceOption", true);

            return "redirect:/admin/warehouse";
        } catch (Exception e) {
            logger.error("Lỗi khi nhập kho sản phẩm với ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Nhập kho thất bại!");
            return "redirect:/admin/warehouse/export-product/" + id;
        }
    }

}



//    @GetMapping("/admin/warehouse/export-product/{id}")
//    public String showExportProductForm(@PathVariable("id") Long id, Model model) {
//        Product product = productService.getProductById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));
//
//        List<ProductVariant> productVariants = productVariantService.findAllByProductId(product.getId());
//        Map<Long, Map<String, Integer>> variantSizeQuantities = new HashMap<>();
//
//        for (ProductVariant variant : productVariants) {
//            List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
//            Map<String, Integer> sizeQuantities = new HashMap<>();
//            for (Size size : sizes) {
//                sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
//            }
//            variantSizeQuantities.put(variant.getId(), sizeQuantities);
//        }
//
//        model.addAttribute("product", product);
//        model.addAttribute("productVariants", productVariants);
//        model.addAttribute("variantSizeQuantities", variantSizeQuantities);
//
//        return "product/export-product";  // giao diện xuất kho
//    }
//
//}
