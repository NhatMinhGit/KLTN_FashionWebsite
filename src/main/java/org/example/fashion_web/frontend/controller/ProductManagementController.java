package org.example.fashion_web.frontend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FilenameUtils;
import org.example.fashion_web.backend.dto.CategoryForm;
import org.example.fashion_web.backend.dto.ProductForm;
import org.example.fashion_web.backend.dto.ProductWithImagesDto;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.*;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class ProductManagementController {

    @Autowired
    private Cloudinary cloudinary;
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
    @Autowired
    private DiscountService discountService;
    @Autowired
    private SizeService sizeService;

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

    @GetMapping("/admin/test-discount")
    public String testApplyDiscount() {
        LocalDate now = LocalDate.now();
        discountService.applyDiscountFor3LowSellingCategories(now.minusDays(30), now);
        return "redirect:/admin/product";
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
    public String addProduct(@ModelAttribute("productForm") ProductForm productForm,
                             @RequestParam(value = "imageUrls", required = false) List<String> imageUrls,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
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
                return brandService.save(newBrand);
            });

            // Tạo product
            Product product = new Product();
            product.setName(productForm.getName());
            product.setPrice(productForm.getPrice());
            product.setDescription(productForm.getDescription());
//            product.setStockQuantity(productForm.getStock_quantity());

            // Set category & brand cho product
            product.setCategory(category);
            product.setBrand(brand);

            productService.save(product);

            //Lưu size
            List<Size> sizes = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : productForm.getSizeQuantities().entrySet()) {
                String sizeName = entry.getKey();
                Integer quantity = entry.getValue();

                // Bỏ qua nếu số lượng là null hoặc 0
                if (quantity == null || quantity <= 0) continue;

                Size size = new Size();
                size.setSizeName(sizeName);
                size.setStockQuantity(quantity);
                size.setProduct(product);

                sizes.add(size);
            }

            sizeService.saveAll(sizes);


            //Lưu ảnh
            if (productForm.getImageFile() != null && !productForm.getImageFile().isEmpty()) {
                System.out.println("Số lượng file: " + productForm.getImageFile().toArray().length);
                for (MultipartFile file : productForm.getImageFile()) {
                    if (!file.isEmpty()) {
                        try {
                            // Ghi log để debug
                            System.out.println("Đang xử lý file: " + file.getOriginalFilename());
                            System.out.println("Kích thước file: " + file.getSize());
                            System.out.println("Loại file: " + file.getContentType());

                            String productName = product.getName();  // Tên sản phẩm
                            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                            String fullFileName = FilenameUtils.getBaseName(file.getOriginalFilename());

                            // Tạo thư mục dựa trên tên sản phẩm
                            String folderPath = "pics/uploads/" + productName;  // Tạo thư mục theo tên sản phẩm

                            // Thay đoạn xử lý file bằng Cloudinary
                            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                                    "public_id", folderPath + "/" + fullFileName,  // Đặt public_id với đường dẫn thư mục sản phẩm
                                    "overwrite", true,
                                    "resource_type", "image"
                            ));

                            // DEBUG kết quả trả về
                            System.out.println("Kết quả upload từ Cloudinary:");
                            for (Object key : uploadResult.keySet()) {
                                System.out.println(key + " : " + uploadResult.get(key));
                            }
                            String imageUrl = (String) uploadResult.get("secure_url");

                            Image image = new Image();
                            image.setProduct(product);
                            image.setImageUri(imageUrl);
                            image.setImageName(file.getOriginalFilename());
                            image.setImageSize((int) file.getSize());
                            image.setImageType(file.getContentType());


                            // Ghi log trước khi lưu
                            System.out.println("=== ĐANG LƯU ẢNH VÀO DB ===");
                            System.out.println("Thông tin ảnh: ");
                            System.out.println("Tên: " + image.getImageName());
                            System.out.println("URI: " + image.getImageUri());
                            System.out.println("Size: " + image.getImageSize());
                            System.out.println("Loại: " + image.getImageType());
                            System.out.println("Product ID: " + (product.getId() != null ? product.getId() : "null"));



                            imageService.save(image);




                        } catch (IOException e) {
                            System.err.println("Lỗi khi lưu ảnh: " + file.getOriginalFilename());
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

            // Truyền lại productForm để giữ dữ liệu đã nhập
            model.addAttribute("productForm", productForm);
            return "product/add-product"; // Không redirect, mà trả về trang nhập form
        }
    }

    // Xem chi tiết sản phẩm theo ID
    @GetMapping("/user/product-detail/{id}")
    public String viewProduct(@PathVariable Long id, Model model,Principal principal) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
        model.addAttribute("user", userDetails);

        Product product = productService.getProductById(id)
                .orElse(null);
        if (product == null) {
            return "redirect:/user"; // Fix đường dẫn redirect
        }
        BigDecimal effectivePrice = discountService.getActiveDiscountForProduct(product)
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
        product.setEffectivePrice(effectivePrice);

        // Lấy danh sách ảnh và chỉ lấy đường dẫn ảnh (imageUrl)
        List<Image> imageList = imageService.findImagesByProductId(product.getId());
        List<String> productImages = imageList.stream()
                .map(Image::getImageUri)
                .filter(imageUri -> imageUri.startsWith("https://res.cloudinary.com"))  // Chỉ lấy ảnh Cloudinary
                .collect(Collectors.toList());

        List<Size> sizes = product.getSizes(); // đã được ánh xạ bởi JPA
        model.addAttribute("sizes", sizes);
        model.addAttribute("product", product);
        model.addAttribute("productImages", productImages);
        return "product/product-detail"; // Đảm bảo có file product/product-detail.html
    }
    // Hàm chuẩn hóa tên sản phẩm thành slug
    public static String encodeForCloudinary(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return input;
        }
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

        // Kiểm tra và lưu ảnh
        String encodedProductName = encodeForCloudinary(product.getName());

        List<Image> imageList = imageService.findImagesByProductId(product.getId());

        List<String> imageUrls = imageList.stream()
                .map(Image::getImageUri)
                .filter(uri -> uri.startsWith("https://res.cloudinary.com"))
                .filter(uri -> uri.contains("/pics/uploads/" + encodedProductName + "/"))
                .collect(Collectors.toList());

        // Chuyển đổi đối tượng Product thành ProductForm để dễ dàng xử lý trong form
        ProductForm productForm = new ProductForm();
        productForm.setProduct_id(product.getId());
        productForm.setBrand_id(product.getBrand().getId());
        productForm.setCategory_id(product.getCategory().getId());
        productForm.setName(product.getName());
        productForm.setPrice(product.getPrice());
        productForm.setDescription(product.getDescription());
//        productForm.setStock_quantity(product.getStockQuantity());

        List<Size> sizes = sizeService.findAllByProductId(product.getId());
        Map<String, Integer> sizeQuantities = new HashMap<>();
        for (Size size : sizes) {
            sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
        }
        productForm.setSizeQuantities(sizeQuantities);


        productForm.setBrand_name(product.getBrand().getName());
        productForm.setCategory_name(product.getCategory().getName());

        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        model.addAttribute("brands", brandService.getAllBrands()); // Lấy danh mục
        model.addAttribute("imageUrls", imageUrls);
        model.addAttribute("sizes", sizes);
        System.out.println(imageUrls);
        model.addAttribute("productForm", productForm);
        return "product/update-products";
    }
    // Hàm tách public_id từ URL của Cloudinary

    public String extractPublicId(String imageUrl) {
        try {
            int index = imageUrl.indexOf("/upload/");
            if (index == -1) return null;

            String publicIdWithVersion = imageUrl.substring(index + 8);
            String[] parts = publicIdWithVersion.split("/", 2);
            if (parts.length < 2) return null;

            String publicIdEncoded = parts[1];
            String publicIdDecoded = URLDecoder.decode(publicIdEncoded, StandardCharsets.UTF_8.name());

            // Xoá đuôi mở rộng như .png, .jpg, .jpeg nếu có
            int dotIndex = publicIdDecoded.lastIndexOf(".");
            if (dotIndex != -1) {
                publicIdDecoded = publicIdDecoded.substring(0, dotIndex);
            }

            return publicIdDecoded;
        } catch (Exception e) {
            System.err.println("Lỗi khi trích xuất public_id từ URL: " + imageUrl);
            e.printStackTrace();
            return null;
        }
    }





    // Xử lý cập nhật ứng viên
    @PostMapping("/admin/products/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("productForm") ProductForm productForm,
                                @RequestParam(value = "deletedImages", required = false) List<String> deletedImages,
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
//            existingProduct.setStockQuantity(productForm.getStock_quantity());
            List<Size> sizes = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : productForm.getSizeQuantities().entrySet()) {
                String sizeName = entry.getKey();
                Integer quantity = entry.getValue();

                // Bỏ qua nếu số lượng là null hoặc 0
                if (quantity == null || quantity <= 0) continue;

                Size size = new Size();
                size.setSizeName(sizeName);
                size.setStockQuantity(quantity);
                size.setProduct(existingProduct);

                sizes.add(size);
            }

            sizeService.saveAll(sizes);
            // Set category & brand cho product
            existingProduct.setCategory(category);
            existingProduct.setBrand(brand);

            // Lưu hình ảnh
            if (productForm.getImageFile() != null) {
                System.out.println("Số lượng file: " + productForm.getImageFile().toArray().length);
                for (MultipartFile file : productForm.getImageFile()) {
                    if (!file.isEmpty()) {
                        try {
                            // Ghi log để debug
                            System.out.println("Đang xử lý file: " + file.getOriginalFilename());
                            System.out.println("Kích thước file: " + file.getSize());
                            System.out.println("Loại file: " + file.getContentType());

                            String productName = existingProduct.getName();  // Tên sản phẩm
                            String baseFileName = FilenameUtils.getBaseName(file.getOriginalFilename());  // không có đuôi


                            // Tạo thư mục dựa trên tên sản phẩm
                            String folderPath = "pics/uploads/" + productName;  // Tạo thư mục theo tên sản phẩm

                            // Thay đoạn xử lý file bằng Cloudinary
                            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                                    "public_id", folderPath + "/" + baseFileName,  // Đặt public_id với đường dẫn thư mục sản phẩm
                                    "overwrite", true,
                                    "resource_type", "image"
                            ));

                            // DEBUG kết quả trả về
                            System.out.println("Kết quả upload từ Cloudinary:");
                            for (Object key : uploadResult.keySet()) {
                                System.out.println(key + " : " + uploadResult.get(key));
                            }
                            String imageUrl = (String) uploadResult.get("secure_url");

                            Image image = new Image();
                            image.setProduct(existingProduct);
                            image.setImageUri(imageUrl);
                            image.setImageName(file.getOriginalFilename());
                            image.setImageSize((int) file.getSize());
                            image.setImageType(file.getContentType());


                            // Ghi log trước khi lưu
                            System.out.println("=== ĐANG LƯU ẢNH VÀO DB ===");
                            System.out.println("Thông tin ảnh: ");
                            System.out.println("Tên: " + image.getImageName());
                            System.out.println("URI: " + image.getImageUri());
                            System.out.println("Size: " + image.getImageSize());
                            System.out.println("Loại: " + image.getImageType());
                            System.out.println("Product ID: " + (existingProduct.getId() != null ? existingProduct.getId() : "null"));



                            imageService.save(image);




                        } catch (IOException e) {
                            System.err.println("Lỗi khi lưu ảnh: " + file.getOriginalFilename());
                            e.printStackTrace();
                        }
                    }
                }
            }


            // Xoá ảnh được chọn
            if (deletedImages != null && !deletedImages.isEmpty()) {
                for (String imageUri : deletedImages) {
                    // Kiểm tra xem URL có phải từ Cloudinary không
                    if (imageUri.startsWith("https://res.cloudinary.com")) {
                        try {
                            // Gọi API của Cloudinary để xoá ảnh
                            System.out.println("Đang xoá ảnh từ Cloudinary với URL: " + imageUri);

                            // Tách public_id từ URL
                            String publicId = extractPublicId(imageUri);
//                            String publicId = "pics/uploads/%C3%81o%20Thun%20Nam/%C3%81o%20s%C6%A1%20mi%20oxford%20nam%20tay%20d%C3%A0i%20form%20fitted%20-%20Smartshirt.png";

                            if (publicId != null) {
                                // Sử dụng public_id để xoá ảnh từ Cloudinary
                                Map<String, String> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                                System.out.println("Kết quả xoá từ Cloudinary: " + result);

                                // Xoá bản ghi trong DB sau khi xoá ảnh trên Cloudinary
                                imageService.deleteImageByImageUri(imageUri);
                            } else {
                                System.err.println("Không thể tách public_id từ URL: " + imageUri);
                            }

                        } catch (IOException e) {
                            System.err.println("Lỗi khi xoá ảnh từ Cloudinary: " + imageUri);
                            e.printStackTrace();
                        }
                    } else {
                        // Nếu không phải ảnh Cloudinary, bỏ qua hoặc xử lý theo yêu cầu
                        System.out.println("Đây không phải là ảnh Cloudinary: " + imageUri);
                    }
                }
            } else {
                System.out.println("Không có ảnh nào để xoá.");
            }




            // Lưu địa chỉ và Sản Phẩm
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
    @GetMapping("/admin/products/search")
    @ResponseBody
    public Page<Product> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return productService.searchProducts(keyword, pageable);
    }





    @GetMapping("/user/shop")
    public String listProducts(
            @RequestParam(value = "category", required = false, defaultValue = "nam") String category,
            Model model, Principal principal) {
//        // Kiểm tra xem có dữ liệu không
//        System.out.println("Danh mục được chọn: " + category);

        // Xử lý thông tin người dùng nếu có
        if (principal != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
            }
        }



        // Lọc sản phẩm theo danh mục
        List<Product> products = productService.getProductsByCategory(category);
//        System.out.println("Số sản phẩm tìm thấy: " + products.size());
//        for (Product p : products) {
//            System.out.println("Sản phẩm: " + p.getName() + " - Giá: " + p.getPrice());
//        }
        // Lấy giảm giá cho toàn bộ danh mục, nếu có
        Optional<ProductDiscount> categoryDiscount = discountService.getActiveDiscountForCategory(true,category);

        for (Product product : products) {
            BigDecimal effectivePrice;
            if (categoryDiscount.isPresent()) {
                // Áp dụng giảm giá cho toàn bộ danh mục
                effectivePrice = discountService.applyDiscount(product.getPrice(), categoryDiscount.get());
            } else {
                effectivePrice = discountService.getActiveDiscountForProduct(product)
                        .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                        .orElse(product.getPrice());
            }
            product.setEffectivePrice(effectivePrice);
        }
        model.addAttribute("products", products);

        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (Product product : products) {
            // Kiểm tra và lưu ảnh
            List<Image> imageList = imageService.findImagesByProductId(product.getId());
            List<String> imageUrls = imageList.stream()
                    .map(Image::getImageUri)
                    .filter(imageUri -> imageUri.startsWith("https://res.cloudinary.com"))  // Chỉ lấy ảnh Cloudinary
                    .collect(Collectors.toList());
            productImages.put(product.getId(), imageUrls);
        }

        model.addAttribute("productImages", productImages);
        model.addAttribute("currentCategory", category); // Lưu danh mục hiện tại để xử lý giao diện

        return "shop"; // Trả về trang shop.html chung
    }

    @GetMapping("/user/shop/search")
    @ResponseBody
    public List<ProductWithImagesDto> searchProducts(@RequestParam("keyword") String keyword) {
        // Lấy danh sách sản phẩm từ service
        List<Product> products = productService.searchByKeyword(keyword);

        // Xử lý ảnh cho từng sản phẩm
        List<ProductWithImagesDto> productDtos = products.stream()
                .map(product -> {
                    // Lấy danh sách ảnh từ imageService
                    List<Image> imageList = imageService.findImagesByProductId(product.getId());
                    List<String> imageUrls = imageList.stream()
                            .map(Image::getImageUri)
                            .filter(url -> url.startsWith("https://res.cloudinary.com")) // Lọc ảnh từ Cloudinary
                            .collect(Collectors.toList());

                    Map<String, Integer> sizeQuantities = new HashMap<>();
                    for (Size size : product.getSizes()) {
                        sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
                    }

                    // Sau đó gán vào DTO
                    return new ProductWithImagesDto(
                            product.getId(),
                            product.getBrand().getId(),
                            product.getCategory().getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getDescription(),
                            sizeQuantities,
                            imageUrls
                    );

                })
                .collect(Collectors.toList());

        // Trả về danh sách sản phẩm dưới dạng JSON
        return productDtos;
    }

}
