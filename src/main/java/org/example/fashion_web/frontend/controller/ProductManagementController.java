package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.CategoryForm;
import org.example.fashion_web.backend.dto.ProductForm;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.Brand;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Image;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ProductManagementController {

    @Autowired
    private final ProductService productService;
    @Autowired
    private final BrandService brandService;
    @Autowired
    private final CategoryService categoryService;
    @Autowired
    private final ImageService imageService;
    @Autowired
    private final UserService userService; // Giả sử bạn có UserService để lấy thông tin người dùng
    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService
    public ProductManagementController(ProductService productService, BrandService brandService, CategoryService categoryService, ImageService imageService, UserService userService) {
        this.productService = productService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.userService = userService;
    }

    @GetMapping("/admin/product")
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
        int totalProducts = productService.getTotalProductsCount();

        // Nếu số sản phẩm > 10, dùng phân trang
        if (totalProducts > 10) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productPage = productService.getAllProducts(pageable);
            model.addAttribute("productPage", productPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "product/products-paging";
        } else {
            // Nếu số sản phẩm <= 15, trả về toàn bộ danh sách
            List<Product> products = productService.getAllProducts();
            model.addAttribute("products", products);
            return "product/products";
        }
    }



    @GetMapping("/admin/products/add-category")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        model.addAttribute("categoriesParent", categoryService.getAllParentCategories()); // Lấy danh mục
        return "product/add-category";
    }

    @PostMapping("/admin/products/add-category")
    public String addCategory(@ModelAttribute("categoryForm") CategoryForm categoryForm, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra và lưu category parent
            Optional<Category> categoryOpt = categoryService.findByName(categoryForm.getParent_category_name());
            Category categoryParent = categoryOpt.orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setId(categoryForm.getParent_category_id());
                return categoryService.save(newCategory);
            });
            // Tạo category
            Category category = new Category();
            category.setName(categoryForm.getCategory_name());
            category.setDescription(categoryForm.getDescription());
            category.setParentCategory(categoryParent);

            categoryService.save(category);

            System.out.println("Thông tin sản phẩm: " + category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục sản phẩm thành công!");
            return "redirect:/admin/product";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm danh mục sản phẩm thất bại!");

            // 🔹 Truyền lại productForm để giữ dữ liệu đã nhập
            model.addAttribute("categoryForm", categoryForm);
            return "product/add-category"; // Không redirect, mà trả về trang nhập form
        }
    }

    @GetMapping("/admin/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        return "product/add-product";
    }

    @PostMapping("/admin/products/add")
    public String addProduct(@ModelAttribute("productForm") ProductForm productForm, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra và lưu category
            Optional<Category> categoryOpt = categoryService.findByName(productForm.getCategory_name());
            Category category = categoryOpt.orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setId(productForm.getCategory_id());
                return categoryService.save(newCategory);
            });

            // Kiểm tra và lưu brand
            Optional<Brand> brandOpt = brandService.findByName(productForm.getBrand_name());
            Brand brand = brandOpt.orElseGet(() -> {
                Brand newBrand = new Brand();
                newBrand.setId(productForm.getBrand_id());
                return brandService.save(newBrand);
            });


            // Tạo product
            Product product = new Product();
            product.setName(productForm.getName());
            product.setPrice(productForm.getPrice());
            product.setDescription(productForm.getDescription());
            product.setStockQuantity(productForm.getStock_quantity());


            // Set category & brand cho product
            product.setCategory(category);
            product.setBrand(brand);

            productService.save(product);
            if (productForm.getImageFile() == null) {
                System.out.println("Không có file nào được gửi lên!");
            } else {
                System.out.println("Số lượng file: " + productForm.getImageFile().toArray().length);
                for (MultipartFile file : productForm.getImageFile()) {
                    System.out.println("Tên file: " + file.getOriginalFilename());
                }
            }
            // Lưu hình ảnh
            if (productForm.getImageFile() != null && !productForm.getImageFile().isEmpty()) {
                // Định nghĩa thư mục lưu ảnh
                String uploadDir = "pics/uploads/";

                // Kiểm tra nếu thư mục chưa tồn tại thì tạo mới
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs(); // Tạo thư mục nếu chưa có
                }

                for (MultipartFile file : productForm.getImageFile()) {
                    if (!file.isEmpty()) {
                        try {
                            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Tránh trùng tên file
                            String filePath = uploadDir + fileName;

                            // Tạo đối tượng Image để lưu vào DB
                            Image image = new Image();
                            image.setProduct(product);
                            image.setImageUri(filePath); // Đường dẫn file để hiển thị
                            image.setImageName(fileName);
                            image.setImageSize((int) file.getSize());
                            image.setImageType(file.getContentType());

                            // Lưu thông tin ảnh vào database
                            imageService.save(image);

                            // Lưu file vào thư mục trên server
                            File destinationFile = new File(uploadPath, fileName);
                            file.transferTo(destinationFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.println("Thông tin sản phẩm: " + product);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/admin/product";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm sản phẩm thất bại!");

            // 🔹 Truyền lại productForm để giữ dữ liệu đã nhập
            model.addAttribute("productForm", productForm);
            return "product/add-product"; // Không redirect, mà trả về trang nhập form
        }
    }
    // 2️⃣ Xem chi tiết sản phẩm theo ID
    @GetMapping("/user/product-detail/{id}")
    public String viewProduct(@PathVariable Long id, Model model,Principal principal) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);

        Product product = productService.getProductById(id)
                .orElse(null);
        if (product == null) {
            return "redirect:/user"; // Fix đường dẫn redirect
        }

        // Lấy danh sách ảnh và chỉ lấy đường dẫn ảnh (imageUrl)
        List<String> productImages = imageService.findImagesByProductId(id)
                .stream()
                .map(Image::getImageUri) // Chỉ lấy URL ảnh
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("productImages", productImages);
        return "product/product-detail"; // Đảm bảo có file product/product-detail.html
    }

    // Hiển thị form cập nhật sản phẩm
    @GetMapping("/admin/products/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

        // Kiểm tra và lưu category
        Optional<Category> categoryOpt = categoryService.findByName(product.getCategory().getName());
        Category category = categoryOpt.orElseGet(() -> {
            Category newCategory = new Category();
            newCategory.setId(product.getCategory().getId());
            return categoryService.save(newCategory);
        });

        // Kiểm tra và lưu brand
        Optional<Brand> brandOpt = brandService.findByName(product.getBrand().getName());
        Brand brand = brandOpt.orElseGet(() -> {
            Brand newBrand = new Brand();
            newBrand.setId(product.getBrand().getId());
            return brandService.save(newBrand);
        });
        // Chuyển đổi đối tượng Product thành ProductForm để dễ dàng xử lý trong form
        ProductForm productForm = new ProductForm();
        productForm.setProduct_id(product.getId());
        productForm.setBrand_id(product.getBrand().getId());
        productForm.setCategory_id(product.getCategory().getId());
        productForm.setName(product.getName());
        productForm.setPrice(product.getPrice());
        productForm.setDescription(product.getDescription());
        productForm.setBrand_name(product.getBrand().getName());
        productForm.setCategory_name(product.getCategory().getName());

        productForm.setImageFile(productForm.getImageFile());
        System.out.println("#"+productForm.getImageFile());

        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        model.addAttribute("productForm", productForm);
        return "product/update-products";
    }
    // Xử lý cập nhật ứng viên
    @PostMapping("/admin/products/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                  @ModelAttribute("productForm") ProductForm productForm,
                                  RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Kiểm tra và lưu category
            Optional<Category> categoryOpt = categoryService.findByName(productForm.getCategory_name());
            Category category = categoryOpt.orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setId(productForm.getCategory_id());
                newCategory.setName(productForm.getCategory_name());
                return categoryService.save(newCategory);
            });

            // Kiểm tra và lưu brand
            Optional<Brand> brandOpt = brandService.findByName(productForm.getBrand_name());
            Brand brand = brandOpt.orElseGet(() -> {
                Brand newBrand = new Brand();
                newBrand.setId(productForm.getBrand_id());
                newBrand.setName(productForm.getBrand_name());
                return brandService.save(newBrand);
            });

            // Tạo product

            existingProduct.setName(productForm.getName());
            existingProduct.setPrice(productForm.getPrice());
            existingProduct.setDescription(productForm.getDescription());
            existingProduct.setStockQuantity(productForm.getStock_quantity());

            // Set category & brand cho product
            existingProduct.setCategory(category);
            existingProduct.setBrand(brand);

            // Lưu hình ảnh
            if (productForm.getImageFile() != null && !productForm.getImageFile().isEmpty()) {
                // Định nghĩa thư mục lưu ảnh
                String uploadDir = "/pics/uploads/";

                // Kiểm tra nếu thư mục chưa tồn tại thì tạo mới
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs(); // Tạo thư mục nếu chưa có
                }

                for (MultipartFile file : productForm.getImageFile()) {
                    if (!file.isEmpty()) {
                        try {
                            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Tránh trùng tên file
                            String filePath = uploadDir + fileName;

                            // Tạo đối tượng Image để lưu vào DB
                            Image image = new Image();
                            image.setProduct(existingProduct);
                            image.setImageUri(filePath); // Đường dẫn file để hiển thị
                            image.setImageName(fileName);
                            image.setImageSize((int) file.getSize());
                            image.setImageType(file.getContentType());

                            // Lưu thông tin ảnh vào database
                            imageService.save(image);

                            // Lưu file vào thư mục trên server
                            File destinationFile = new File(uploadPath, fileName);
                            file.transferTo(destinationFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            // Lưu địa chỉ và ứng viên
            productService.updateProduct(id, existingProduct);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ứng viên thành công!");
            return "redirect:/admin/product";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật ứng viên thất bại!");
            return "redirect:/admin/products/edit/" + id;
        }
    }

    // Xử lý xóa ứng viên
    @GetMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa ứng viên thành công!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa ứng viên thất bại!");
        }
        return "redirect:/admin/product";
    }
    @GetMapping("/user/shop")
    public String listProducts(
            @RequestParam(value = "category", required = false, defaultValue = "nam") String category,
            Model model, Principal principal) {
        // Kiểm tra xem có dữ liệu không
        System.out.println("Danh mục được chọn: " + category);

        // Xử lý thông tin người dùng nếu có
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            }
        }

        // Lọc sản phẩm theo danh mục
        List<Product> products = productService.getProductsByCategory(category);
        System.out.println("Số sản phẩm tìm thấy: " + products.size());
        for (Product p : products) {
            System.out.println("Sản phẩm: " + p.getName() + " - Giá: " + p.getPrice());
        }
        model.addAttribute("products", products);

        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (Product product : products) {
            List<Image> images = imageService.findImagesByProductId(product.getId());
            List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
            productImages.put(product.getId(), imageUrls);
        }

        model.addAttribute("productImages", productImages);
        model.addAttribute("currentCategory", category); // Lưu danh mục hiện tại để xử lý giao diện

        return "shop"; // Trả về trang shop.html chung
    }

}
