package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.ProductDto;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.services.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách các ứng viên không phân trang
    @GetMapping("/list")
    public String listCandidates(Model model) {
        List<Product> products = productService.getAllProduct();
        model.addAttribute("products", products);
        return "products";
    }
    @GetMapping("/list-paged")
    public String listCandidatesPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.getAllProduct(pageable);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "products-paging";
    }

    // Hiển thị form thêm ứng viên mới
    @GetMapping("/add")
    public String showAddCandidateForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        return "add-product";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("productDto") ProductDto productDto, RedirectAttributes redirectAttributes) {
        try{
            // Tạo Category mới
            Category category = new Category();
            category.setCategoryName(productDto.getCategory().getCategoryName());
            category.setDescription(productDto.getCategory().getDescription());

            categoryService.createCategory(category);

            // Tạo Product mới
            Product product = new Product();
            product.setName(productDto.getName());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            product.setStockQuantity(productDto.getStockQuantity());
            product.setCategory(category);

            productService.createProduct(product);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm ứng viên thành công!");
            return "redirect:/products/list";
        } catch (Exception e){
            e.printStackTrace();
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Thêm ứng viên thất bại!");
        return "redirect:/products/add";
    }
}
