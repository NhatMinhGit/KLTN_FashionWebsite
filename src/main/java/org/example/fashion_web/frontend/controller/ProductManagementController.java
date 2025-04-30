package org.example.fashion_web.frontend.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FilenameUtils;
import org.example.fashion_web.backend.dto.*;
import org.example.fashion_web.backend.exceptions.ResourceNotFoundException;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.ImageRepository;
import org.example.fashion_web.backend.repositories.ProductVariantRepository;
import org.example.fashion_web.backend.repositories.SizeRepository;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.sql.Timestamp;
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
    @Autowired
    private FeedBackService feedBackService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private final ProductVariantService productVariantService;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;



    public ProductManagementController(ProductService productService, BrandService brandService, CategoryService categoryService, ImageService imageService, UserService userService, ProductVariantService productVariantService) {
        this.productService = productService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.imageService = imageService;
        this.userService = userService;
        this.productVariantService = productVariantService;
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

            // Truyền lại productForm để giữ dữ liệu đã nhập
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
                             @RequestParam("variantImages") List<MultipartFile> imageFiles,
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

            // Tạo sản phẩm và gán category & brand
            Product product = new Product();
            product.setName(productForm.getName());
            product.setPrice(productForm.getPrice());
            product.setDescription(productForm.getDescription());
            product.setCategory(category);
            product.setBrand(brand);

            productService.save(product);

            // === Xử lý tạo variant & size ===
            List<ProductVariant> variants = new ArrayList<>();
            List<String> colors = productForm.getColor(); // Danh sách màu từ form

            // Lặp qua từng màu để tạo ProductVariant
            for (String color : colors) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(color);
                variant.setProduct(product);
                productVariantService.save(variant);

                // Lặp qua các size và số lượng từ form để tạo Size cho variant hiện tại
                for (Map.Entry<String, Integer> entry : productForm.getSizeQuantities().entrySet()) {
                    String sizeName = entry.getKey();
                    Integer quantity = entry.getValue();

                    if (quantity != null && quantity > 0) {
                        Size size = new Size();
                        size.setSizeName(sizeName);
                        size.setStockQuantity(quantity);
                        size.setProductVariant(variant); // Thiết lập liên kết với variant
                        sizeService.save(size);
                    }
                }
                variants.add(variant);
            }

            // Lưu ảnh
            if (!imageFiles.isEmpty()) {
                uploadImages(variants, imageFiles);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/admin/product";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm sản phẩm thất bại!");
            model.addAttribute("productForm", productForm); // Giữ lại form nhập liệu
            return "product/add-product"; // Trả về trang form nếu có lỗi
        }
    }

    // Tách riêng việc upload ảnh để code dễ đọc và dễ bảo trì
    private void uploadImages(List<ProductVariant> variants, List<MultipartFile> imageFiles) {
        for (ProductVariant variant : variants) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String productName = variant.getProduct().getName();
                        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                        String fullFileName = FilenameUtils.getBaseName(file.getOriginalFilename());
                        String folderPath = "pics/uploads/" + productName;

                        // Upload file lên Cloudinary
                        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                                "public_id", folderPath + "/" + fullFileName,
                                "overwrite", true,
                                "resource_type", "image"
                        ));

                        String imageUrl = (String) uploadResult.get("secure_url");

                        // Tạo đối tượng Image và gán thông tin
                        Image image = new Image();
                        image.setProductVariant(variant); // Gán ProductVariant cho Image
                        image.setImageUri(imageUrl); // Đặt URI ảnh từ Cloudinary
                        image.setImageName(file.getOriginalFilename()); // Đặt tên file ảnh
                        image.setImageSize((int) file.getSize()); // Đặt kích thước ảnh
                        image.setImageType(file.getContentType()); // Đặt loại file ảnh

                        // Lưu ảnh vào cơ sở dữ liệu
                        imageService.save(image);

                    } catch (IOException e) {
                        System.err.println("Lỗi khi lưu ảnh: " + file.getOriginalFilename());
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    // Xem chi tiết sản phẩm theo ID
    @GetMapping("/user/product-detail/{id}")
    public String viewProduct(@PathVariable Long id, Model model, Principal principal) {
        try {
            // Lấy thông tin người dùng
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            model.addAttribute("user", userDetails);
            // Lấy sản phẩm theo ID
            Product product = productService.getProductById(id).orElse(null);
            if (product == null) {
                return "redirect:/user"; // Nếu không có sản phẩm, redirect
            }

            // Áp dụng discount nếu có
            BigDecimal effectivePrice = discountService.getActiveDiscountForProduct(product)
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
            product.setEffectivePrice(effectivePrice);

            // Lấy các variant của sản phẩm
            List<ProductVariant> productVariants = productVariantService.findAllByProductId(product.getId());

            if (productVariants.isEmpty()) {
                return "redirect:/user"; // Redirect nếu không có variants
            }

//            // Khởi tạo Map để gom ảnh theo màu sắc
//            Map<String, List<String>> imagesByColor = new HashMap<>();
//
//            // Lấy ảnh của từng variant và gom theo màu
//            for (ProductVariant productVariant : productVariants) {
//                List<Image> imageList = imageService.findImagesByProductVariantId(productVariant.getId());
//                if (imageList != null && !imageList.isEmpty()) {
//                    List<String> imageUrls = imageList.stream()
//                            .map(Image::getImageUri)
//                            .filter(imageUri -> imageUri.startsWith("https://res.cloudinary.com"))
//                            .toList();
//
//                    // Gom ảnh theo màu sắc
//                    String color = productVariant.getColor();
//                    if (color != null) {
//                        imagesByColor.computeIfAbsent(color, k -> new ArrayList<>()).addAll(imageUrls);
//                    }
//                }
//            }
//
//            // Truyền ảnh vào model
//            model.addAttribute("imagesByColor", imagesByColor);
            Map<String, List<String>> imagesByColor = new HashMap<>();
            Map<String, Long> variantIdByColor = new HashMap<>();

            for (ProductVariant productVariant : productVariants) {
                List<Image> imageList = imageService.findImagesByProductVariantId(productVariant.getId());
                if (imageList != null && !imageList.isEmpty()) {
                    List<String> imageUrls = imageList.stream()
                            .map(Image::getImageUri)
                            .filter(imageUri -> imageUri.startsWith("https://res.cloudinary.com"))
                            .toList();

                    String color = productVariant.getColor();
                    if (color != null) {
                        imagesByColor.computeIfAbsent(color, k -> new ArrayList<>()).addAll(imageUrls);

                        // Gán variantId đầu tiên cho mỗi màu (nếu chưa có)
                        variantIdByColor.putIfAbsent(color, productVariant.getId());
                    }
                }
            }

            model.addAttribute("imagesByColor", imagesByColor);
            model.addAttribute("variantIdByColor", variantIdByColor);

            Map<String, List<SizeInfo>> sizesByColor = new HashMap<>();

            for (ProductVariant variant : productVariants) {
                if (variant == null || variant.getColor() == null) {
                    System.out.println("Skipping null variant or color for variant ID: " + (variant != null ? variant.getId() : "unknown"));
                    continue;
                }
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                if (sizes == null) {
                    System.out.println("Sizes are null for variant ID: " + variant.getId());
                    continue;
                }
                String color = variant.getColor();
                List<SizeInfo> sizeInfos = sizesByColor.getOrDefault(color, new ArrayList<>());

                for (Size size : sizes) {
                    if (size == null || size.getSizeName() == null) {
                        System.out.println("Skipping null size or size name for variant ID: " + variant.getId());
                        continue;
                    }
                    SizeInfo sizeInfo = new SizeInfo(color, size.getSizeName(), size.getStockQuantity());
                    sizeInfos.add(sizeInfo);
                }

                sizesByColor.put(color, sizeInfos);
            }

// Tiếp tục với các đoạn mã bên dưới
            model.addAttribute("sizesByColor", sizesByColor);

// Lấy feedbacks của sản phẩm
            List<Feedback> feedbacks = feedBackService.findByProductIdOrderByCreateAtDesc(id);
            model.addAttribute("feedbacks", feedbacks);

// Lấy màu sắc đầu tiên từ sizesByColor (màu mặc định)
            String selectedColor = sizesByColor.keySet().stream().findFirst().orElse("Red"); // mặc định "Red"
            System.out.println("sizesByColor keys:");
            sizesByColor.keySet().forEach(System.out::println);
            System.out.println("Selected color: " + selectedColor);
            System.out.println("Sizes by color: " + sizesByColor);

// Thêm màu sắc đã chọn vào model
            model.addAttribute("selectedColor", selectedColor);

// Thêm sản phẩm vào model
            model.addAttribute("product", product);
            return "product/product-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while retrieving the product: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================" + e.getMessage());
            return "redirect:/user";
        }
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

        // Chuyển đổi đối tượng Product thành ProductForm để dễ dàng xử lý trong form
        ProductForm productForm = new ProductForm();
        productForm.setProduct_id(product.getId());
        productForm.setBrand_id(product.getBrand().getId());
        productForm.setCategory_id(product.getCategory().getId());
        productForm.setName(product.getName());
        productForm.setPrice(product.getPrice());
        productForm.setDescription(product.getDescription());

        // Lấy các variant của sản phẩm
        List<ProductVariant> productVariants = productVariantService.findAllByProductId(product.getId());

        // Dữ liệu về kích thước và số lượng cho các variant
        Map<Long, Map<String, Integer>> variantSizeQuantities = new HashMap<>();
        Map<Long, List<Image>> variantImagesMap = new HashMap<>();
        for (ProductVariant variant : productVariants) {
            List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
            Map<String, Integer> sizeQuantities = new HashMap<>();
            for (Size size : sizes) {
                sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
            }
            variantSizeQuantities.put(variant.getId(), sizeQuantities);

            // Lọc ảnh theo đường dẫn Cloudinary
            String encodedProductName = encodeForCloudinary(product.getName());
            List<Image> variantImages = imageService.findImagesByProductVariantId(variant.getId());

            // Lọc các ảnh từ Cloudinary có đường dẫn đúng
            List<Image> cloudinaryImages = variantImages.stream()
                    .filter(image -> image.getImageUri().startsWith("https://res.cloudinary.com"))
                    .filter(image -> image.getImageUri().contains("/pics/uploads/" + encodedProductName + "/"))
                    .collect(Collectors.toList());

            // Lưu ảnh đã lọc vào variantImagesMap
            variantImagesMap.put(variant.getId(), cloudinaryImages);
        }


        // Truyền vào model
        model.addAttribute("variantImagesMap", variantImagesMap);
        model.addAttribute("variantSizeQuantities", variantSizeQuantities);

        // Thêm thông tin variants vào form
        // productForm.setVariantSizes(variantSizeQuantities);

        productForm.setBrand_name(product.getBrand().getName());
        productForm.setCategory_name(product.getCategory().getName());

        model.addAttribute("categories", categoryService.getAllChildrenCategories()); // Lấy danh mục
        model.addAttribute("brands", brandService.getAllBrands()); // Lấy thương hiệu
        model.addAttribute("productVariants", productVariants); // Các variants của sản phẩm
        model.addAttribute("variantSizeQuantities", variantSizeQuantities); // Các kích thước và số lượng của variants
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




    private void updateProductCategoryAndBrand(ProductForm productForm, Product existingProduct) {
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

        existingProduct.setBrand(brand);
        existingProduct.setCategory(category);
    }
    private void updateProductVariants(ProductForm productForm, Product existingProduct) {
        // Xử lý variant
        // Tách logic xử lý variant từ hàm chính vào đây
        // Lấy dữ liệu từ form
        Map<Long, Map<String, Integer>> variantSizesForm = Optional.ofNullable(productForm.getVariantSizes())
                .orElse(new HashMap<>());
        Map<Long, String> variantColors = productForm.getVariantColors(); // Mảng màu sắc từ form

        // Lấy danh sách các variant hiện có từ DB
        List<ProductVariant> dbVariants = Optional.ofNullable(productVariantRepository.findByProductId(existingProduct.getId()))
                .orElse(new ArrayList<>());

        for (Map.Entry<Long, Map<String, Integer>> variantEntry : variantSizesForm.entrySet()) {
            Long variantId = variantEntry.getKey();
            Map<String, Integer> colorMap = variantEntry.getValue(); // Map<SizeName, Quantity>

            // Kiểm tra nếu variant đã tồn tại trong DB
            ProductVariant variant = dbVariants.stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElse(null);

            if (variant == null) {
                // Nếu variant chưa có trong DB, tạo mới variant
                variant = new ProductVariant();
                variant.setProduct(existingProduct); // Gắn sản phẩm hiện tại
                // Cập nhật màu sắc cho variant nếu có
                String color = variantColors.get(variantId); // Lấy màu sắc từ form theo ID variant
                variant.setColor(color); // Cập nhật màu sắc
                productVariantService.save(variant); // Lưu variant mới

            }

            // Cập nhật hoặc tạo mới Size cho từng variant
            List<Size> sizeList = sizeRepository.findByProductVariantId(variant.getId()); // Lấy danh sách sizes hiện tại của variant

            for (Map.Entry<String, Integer> sizeEntry : colorMap.entrySet()) {
                String sizeName = sizeEntry.getKey(); // Tên kích thước
                Integer quantity = sizeEntry.getValue(); // Số lượng

                if (quantity == null || quantity <= 0) continue; // Nếu số lượng không hợp lệ, bỏ qua

                // Kiểm tra nếu Size đã tồn tại cho variant
                Size sizeEntity = sizeList.stream()
                        .filter(s -> s.getSizeName().equals(sizeName))
                        .findFirst()
                        .orElse(null);

                if (sizeEntity == null) {
                    // Nếu size chưa có, tạo mới Size
                    sizeEntity = new Size();
                    sizeEntity.setSizeName(sizeName);
                    sizeEntity.setStockQuantity(quantity);
                    sizeEntity.setProductVariant(variant); // Gắn variant vào size mới
                    sizeRepository.save(sizeEntity); // Lưu Size mới
                } else {
                    // Nếu size đã có, cập nhật số lượng
                    sizeEntity.setStockQuantity(quantity);
                    sizeService.updateSize(sizeEntity.getId(), sizeEntity); // Cập nhật Size
                }
            }
        }
    }

    // Xử lý cập nhật sản phẩm
    @PostMapping("/admin/products/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("productForm") ProductForm productForm,
                                @RequestParam MultiValueMap<String, MultipartFile> variantImages,
                                @RequestParam MultiValueMap<String, String> variantSizes,
                                @RequestParam(value = "deletedImages", required = false) List<String> deletedImages,
                                @RequestParam(value = "deletedVariantsColor", required = false) List<Long> deletedVariantsColor, // Danh sách variant ID cần xóa
                                RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại với ID: " + id));

            // Tách các phần logic ra thành các phương thức riêng
            updateProductCategoryAndBrand(productForm, existingProduct);
            updateProductVariants(productForm, existingProduct);

            // Tạo product
            existingProduct.setName(productForm.getName());
            existingProduct.setPrice(productForm.getPrice());
            existingProduct.setDescription(productForm.getDescription());
            // Lưu địa chỉ và Sản Phẩm
            productService.updateProduct(id, existingProduct);

            // Lưu hình ảnh cho từng biến thể
            for (Map.Entry<String, List<MultipartFile>> entry : variantImages.entrySet()) {
                String key = entry.getKey(); // "variantImages[123]"
                Long variantId = Long.valueOf(key.replaceAll("[^0-9]", "")); // Lấy ID variant từ key

                Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
                if (variantOpt.isPresent()) {
                    ProductVariant variant = variantOpt.get();

                    // Upload hình ảnh cho biến thể
                    for (MultipartFile file : entry.getValue()) {
                        if (!file.isEmpty()) {
                            // Upload lên Cloudinary
                            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                                    "public_id", "pics/uploads/" + existingProduct.getName() + "/" + file.getOriginalFilename(),
                                    "overwrite", true,
                                    "resource_type", "image"
                            ));

                            String imageUrl = (String) uploadResult.get("secure_url");

                            Image image = new Image();
                            image.setProductVariant(variant);
                            image.setImageUri(imageUrl);
                            image.setImageName(file.getOriginalFilename());
                            image.setImageSize((int) file.getSize());
                            image.setImageType(file.getContentType());

                            imageService.save(image); // Lưu ảnh
                        }
                    }
                } else {
                    System.err.println("Không tìm thấy variant với ID: " + variantId);
                }
            }
            // Xử lý xóa variant
            if (deletedVariantsColor != null && !deletedVariantsColor.isEmpty()) {
                for (Long variantId : deletedVariantsColor) {
                    Optional<ProductVariant> variantOpt = productVariantRepository.findById(variantId);
                    if (variantOpt.isPresent()) {
                        ProductVariant variant = variantOpt.get();

                        // Tìm và xóa các Size có variant_id tương ứng với variantId
                        List<Size> sizes = sizeRepository.findByProductVariantId(variantId); // Tạo phương thức findByVariantId trong SizeRepository
                        for (Size size : sizes) {
                            sizeRepository.delete(size);  // Xóa size khỏi cơ sở dữ liệu
                            System.out.println("Đã xóa size với ID: " + size.getId());
                        }
                        // Tìm và xóa các images có variant_id tương ứng với variantId
                        List<Image> images = imageRepository.findAllByProductVariantId(variantId); // Tạo phương thức findByVariantId trong SizeRepository
                        for (Image image : images) {
                            imageRepository.delete(image);  // Xóa size khỏi cơ sở dữ liệu
                            System.out.println("Đã xóa size với ID: " + image.getId());
                        }
                        // Xóa variant này khỏi cơ sở dữ liệu
                        productVariantRepository.deleteById(variantId);
                        System.out.println("Đã xóa variant màu với ID: " + variantId);
                    } else {
                        System.err.println("Không tìm thấy variant với ID: " + variantId);
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
            List<ProductVariant> productVariants = productVariantService.findAllByProductId(id);
            for (ProductVariant variant : productVariants) {
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());

                // Nếu có các Size liên kết, tiến hành xóa
                if (!sizes.isEmpty()) {
                    sizeRepository.deleteAll(sizes);  // Xóa các Size liên kết
                }
                List<Image> images = imageService.findImagesByProductVariantId(variant.getId());
                if (!images.isEmpty()) {
                    imageRepository.deleteAll(images);  // Xóa các Size liên kết
                }
            }

            productVariantRepository.deleteAll(productVariants);
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

    @PostMapping("user/product-detail/{id}/comments")
    public String addComment(@PathVariable("id") Long productId,
                             @RequestParam("comment") String content,
                             @RequestParam("rating") int rating,
                             Principal principal) {

        User user = userService.findByEmail(principal.getName());
        System.out.println(user.toString());
        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
//        // Tìm feedback của sản phẩm
//        Feedback feedback = feedBackService.findByProductId(productId)
//                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
//        feedback.setOrderItem(orderItem);
//        comment.setFeedback(feedback);
        feedback.setProduct(product); // Gắn comment với sản phẩm luôn
        feedback.setComment(content);
        feedback.setRating(rating);
        feedback.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        feedBackService.save(feedback);

        return "redirect:/user/product-detail/" + productId;
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
            List<Image> imageList = imageService.findImagesByProductVariantId(product.getId());
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
        // Retrieve the list of products from the service
        List<Product> products = productService.searchByKeyword(keyword);

        // Process images and variants for each product
        List<ProductWithImagesDto> productDtos = products.stream()
                .map(product -> {
                    // Retrieve the list of images from imageService
                    List<Image> imageList = imageService.findImagesByProductVariantId(product.getId());
                    // Filter images from Cloudinary and only take valid URLs
                    List<String> imageUrls = imageList.stream()
                            .map(Image::getImageUri)
                            .filter(url -> url.startsWith("https://res.cloudinary.com")) // Only take images from Cloudinary
                            .collect(Collectors.toList());

                    // Create a map to hold size and quantity information for each product
                    Map<String, Integer> sizeQuantities = new HashMap<>();

                    // Use sizeService to find sizes by product variant ID
                    for (ProductVariant variant : product.getVariants()) {
                        List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                        for (Size size : sizes) {
                            sizeQuantities.put(size.getSizeName(), size.getStockQuantity());
                        }
                    }

                    // Create a map to hold variant information (color -> size -> quantity)
                    Map<String, Map<String, Integer>> variantSizes = new HashMap<>();
                    for (ProductVariant variant : product.getVariants()) {
                        Map<String, Integer> sizeStockMap = new HashMap<>();
                        List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                        for (Size size : sizes) {
                            sizeStockMap.put(size.getSizeName(), size.getStockQuantity());
                        }
                        variantSizes.put(variant.getColor(), sizeStockMap);
                    }

                    // Create a list of product variants
                    List<ProductVariantDto> productVariants = product.getVariants().stream()
                            .map(variant -> new ProductVariantDto(
                                    variant.getId(),
                                    variant.getColor(),
                                    sizeService.findAllByProductVariantId(variant.getId()).stream()
                                            .map(size -> new SizeDto(size.getSizeName(), size.getStockQuantity()))
                                            .collect(Collectors.toList())
                            ))
                            .collect(Collectors.toList());

                    // Assign to DTO
                    return new ProductWithImagesDto(
                            product.getId(),
                            product.getBrand().getId(),
                            product.getCategory().getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getDescription(),
                            sizeQuantities,  // Add size and quantity information
                            imageUrls,       // Add images to DTO
                            productVariants   // Add variants to DTO
                    );
                })
                .collect(Collectors.toList());

        // Return the list of products as JSON
        return productDtos;
    }

}
