package org.example.fashion_web.frontend.controller;

import jakarta.validation.Valid;
import org.example.fashion_web.backend.dto.DiscountForm;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.example.fashion_web.backend.services.CategoryService;
import org.example.fashion_web.backend.services.DiscountService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class DiscountManagementController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/admin/discounts/add")
    public String showAddDiscountForm(Model model) {
        model.addAttribute("discountForm", new DiscountForm());
        model.addAttribute("products", productService.getAllProducts()); // Lấy danh mục
        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        return "discount/add-discount";
    }

    @PostMapping("/admin/discounts/add")
    public String createDiscount(@Valid @ModelAttribute("discountForm") DiscountForm form,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) Long productId,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam String applyType) {
        if (bindingResult.hasErrors()) {
            return "discount/add-discount";  // <-- phải đúng path ở return
        }

        ProductDiscount discount = new ProductDiscount();
        discount.setName(form.getName());
        discount.setDiscountPercent(form.getDiscountPercent());
        discount.setStartTime(form.getStartDate().atStartOfDay());
        discount.setEndTime(form.getEndDate().atStartOfDay());
        discount.setActive(form.getStatus());

        if ("product".equals(applyType) && productId != null) {
            productService.findById(productId).ifPresent(discount::setProduct);
        } else if ("category".equals(applyType) && categoryId != null) {
            categoryService.findById(categoryId).ifPresent(discount::setCategory);
        }

        discountService.save(discount);
        return "redirect:/admin/discount";
    }



    @GetMapping("/admin/discount")
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

        // Lấy tổng số sản phẩm
        int totalDiscounts = discountService.getTotalDiscountsCount();

        // Nếu số sản phẩm > 10, dùng phân trang
        if (totalDiscounts > 10) {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDiscount> discountPage = discountService.getAllDiscounts(pageable);
            model.addAttribute("discountPage", discountPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "discount/discounts-paging";
        } else {
            // Nếu số sản phẩm <= 15, trả về toàn bộ danh sách
            List<ProductDiscount> discounts = discountService.getAllDiscounts();
            model.addAttribute("discounts", discounts);
            return "discount/discounts";
        }
    }
    // Hiển thị form cập nhật giảm giá
    @GetMapping("/admin/discounts/edit/{id}")
    public String showEditDiscountForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductDiscount discount = discountService.getDiscountById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảm giá với ID: " + id));

            // Khởi tạo biến để gán vào giảm giá
            Category category = null;
            Product product = null;

            // Xử lý category nếu có
            if (discount.getCategory() != null) {
                Long categoryId = discount.getCategory().getId();
                if (categoryId != null) {
                    category = categoryService.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy danh mục với ID: " + categoryId));
                } else {
                    System.out.println("Category có nhưng thiếu ID – bỏ qua xử lý category.");
                }
            } else {
                System.out.println("Category null – bỏ qua xử lý category.");
            }

            // Xử lý product nếu có
            if (discount.getProduct() != null) {
                Long productId = discount.getProduct().getId();
                if (productId != null) {
                    product = productService.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy sản phẩm với ID: " + productId));
                } else {
                    System.out.println("Product có nhưng thiếu ID – bỏ qua xử lý product.");
                }
            } else {
                System.out.println("Product null – bỏ qua xử lý product.");
            }

            // Kiểm tra nếu cả category và product đều null thì không hợp lệ
            if (category == null && product == null) {
                throw new IllegalArgumentException("Cần có ít nhất sản phẩm hoặc danh mục để áp dụng giảm giá.");
            }

            // Gán lại vào discount
            discount.setCategory(category);
            discount.setProduct(product);



            // Tạo DiscountForm
            DiscountForm discountForm = new DiscountForm();
            discountForm.setId(discount.getId());
            // Gán productId và categoryId nếu có
            if (product != null) {
                discountForm.setProductId(product.getId());
                discountForm.setProductName(product.getName());
            }
            if (category != null) {
                discountForm.setCategoryId(category.getId());
                discountForm.setCategoryName(category.getName());
            }
            discountForm.setName(discount.getName());
            discountForm.setDiscountPercent(discount.getDiscountPercent());
            discountForm.setStartDate(discount.getStartTime().toLocalDate());
            discountForm.setEndDate(discount.getEndTime().toLocalDate());

            // Gán productId và categoryId nếu có
            if (product != null) {
                discountForm.setProductId(product.getId());
                discountForm.setProductName(product.getName());
            }
            if (category != null) {
                discountForm.setCategoryId(category.getId());
            }

            // Gửi dữ liệu tới view
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllChildrenCategories());
            model.addAttribute("discountForm", discountForm);
            return "discount/update-discount";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hiển thị form cập nhật giảm giá.");
            return "redirect:/admin/discount";
        }
    }


    // Xử lý form cập nhật giảm giá
    @PostMapping("/admin/discounts/edit/{id}")
    public String updateDiscount(@PathVariable("id") Long id,
                                 @ModelAttribute("discountForm") DiscountForm discountForm,
                                 RedirectAttributes redirectAttributes) {
        try {
            ProductDiscount existingDiscount = discountService.getDiscountById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảm giá với ID: " + id));
            // Đảm bảo discountForm chứa id
            System.out.println("ID nhận được: " + id);
            // Xử lý product nếu có
            Product product = null;
            if (discountForm.getProductId() != null) {
                product = productService.getProductById(discountForm.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + discountForm.getProductId()));
            }

            // Xử lý category nếu có
            Category category = null;
            if (discountForm.getCategoryId() != null) {
                category = categoryService.findById(discountForm.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + discountForm.getCategoryId()));
            }

            // Nếu cả product và category đều null thì không cho phép
            if (product == null && category == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cần chọn ít nhất một sản phẩm hoặc danh mục.");
                return "redirect:/admin/discounts/edit/" + id;
            }

            if (category != null) {
                existingDiscount.setCategory(category); // Đảm bảo gán lại
            }
            if (product != null) {
                existingDiscount.setProduct(product); // Đảm bảo gán lại
            }

            // Cập nhật thông tin giảm giá
            existingDiscount.setProduct(product); // Nếu product null thì sẽ xóa liên kết
            existingDiscount.setCategory(category); // Nếu category null thì sẽ xóa liên kết
            existingDiscount.setDiscountPercent(discountForm.getDiscountPercent());
            existingDiscount.setStartTime(discountForm.getStartDate().atStartOfDay());
            existingDiscount.setEndTime(discountForm.getEndDate().atStartOfDay());
            System.out.println("Product ID nhận được: " + discountForm.getProductId());
            System.out.println("Category ID nhận được: " + discountForm.getCategoryId());
            System.out.println("Discount Percent: " + discountForm.getDiscountPercent());
            System.out.println("Start Time: " + discountForm.getStartDate());
            System.out.println("End Time: " + discountForm.getEndDate());

            discountService.save(existingDiscount);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giảm giá thành công!");
            return "redirect:/admin/discount";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/discounts/edit/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật giảm giá thất bại! Vui lòng thử lại sau.");
            return "redirect:/admin/discounts/edit/" + id;
        }
    }


}
