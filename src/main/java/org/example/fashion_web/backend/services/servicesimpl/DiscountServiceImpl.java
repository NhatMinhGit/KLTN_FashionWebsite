package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.CategoryRevenueDto;
import org.example.fashion_web.backend.dto.DiscountDto;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.Category;
import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.ProductDiscount;
import org.example.fashion_web.backend.repositories.DiscountRepository;
import org.example.fashion_web.backend.repositories.OrderItemRepository;
import org.example.fashion_web.backend.repositories.ProductRepository;
import org.example.fashion_web.backend.services.DiscountService;
import org.example.fashion_web.backend.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Override
    public ProductDiscount save(ProductDiscount productDiscount) {
        return discountRepository.save(productDiscount);
    }

    @Override
    public Optional<ProductDiscount> findById(Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public void updateDiscountStatuses() {
        discountRepository.activateValidDiscounts();
        discountRepository.deactivateInvalidDiscounts();
    }

    @Override
    public Page<DiscountDto> searchDiscounts(String keyword, Pageable pageable) {
        Page<ProductDiscount> discountPage;

        if (keyword.isEmpty()) {
            discountPage = discountRepository.findAll(pageable);
        } else {
            discountPage = discountRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }

        return discountPage.map(discount -> {
            DiscountDto dto = new DiscountDto();
            dto.setId(discount.getId());
            dto.setName(discount.getName());
            dto.setDiscountPercent(discount.getDiscountPercent());
            dto.setStartTime(discount.getStartTime());
            dto.setEndTime(discount.getEndTime());

            if (discount.getProduct() != null) {
                dto.setProductName(discount.getProduct().getName());
            }
            if (discount.getCategory() != null) {
                dto.setCategoryName(discount.getCategory().getName());
            }

            return dto;
        });
    }



    @Override
    public Optional<ProductDiscount> getActiveDiscountForProduct(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return discountRepository.findFirstByProductAndStartTimeBeforeAndEndTimeAfter(
                product, now, now
        );
    }

    @Override
    public List<ProductDiscount> getActiveDiscountsForProduct(Product product) {
        return discountRepository.findByProductIdAndActiveTrue(product.getId());
    }

    @Override
    public List<ProductDiscount> getActiveDiscountsForCategory(Category category) {
        return discountRepository.findByCategoryIdAndActiveTrue(category.getId());
    }

    @Override
    public List<BigDecimal> getEffectivePrices(List<Product> products) {
        List<BigDecimal> effectivePrices = new ArrayList<>();

        for (Product product : products) {
            BigDecimal effectivePrice = this.getActiveDiscountForProduct(product)
                    .map(discount -> this.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
            effectivePrices.add(effectivePrice);
        }

        return effectivePrices;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal price, ProductDiscount discount) {
        if (discount.getDiscountPercent() != null) {
            BigDecimal discountAmount = price
                    .multiply(BigDecimal.valueOf(discount.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
            return price.subtract(discountAmount);
        }
        return price;
    }

    @Override
    public void applyDiscountFor10LowSellingProducts(LocalDate startDate, LocalDate endDate) {
        List<ProductRevenueDto> result = orderItemRepository.findTopProductsByLowestRevenue(startDate, endDate);
        List<ProductRevenueDto> lowRevenueProducts = result.size() > 10 ? result.subList(0, 10) : result;

        for (ProductRevenueDto dto : lowRevenueProducts) {
            Optional<Product> productOpt = productRepository.findById(dto.getId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                ProductDiscount discount = new ProductDiscount();
                discount.setProduct(product);
                discount.setDiscountPercent(10);
                discount.setActive(true);
                discount.setName("Giảm giá sản phẩm bán chậm");
                discount.setStartTime(LocalDateTime.now());
                discount.setEndTime(LocalDateTime.now().plusDays(7)); // Giảm trong 7 ngày

                discountRepository.save(discount);
            }
        }
    }

    @Override
    public void applyDiscountFor3LowSellingCategories(LocalDate startDate, LocalDate endDate) {
        List<CategoryRevenueDto> result = orderItemRepository.findTopCategoriesByLowestRevenue(startDate, endDate);
        List<CategoryRevenueDto> lowRevenueCategories = result.size() > 3 ? result.subList(0, 3) : result;
        List<Product> products = new ArrayList<>();

        for (CategoryRevenueDto dto : lowRevenueCategories) {
            List<Product> productsInCategory = productService.getProductsByCategory(dto.getName());
            products.addAll(productsInCategory);
        }

        for (Product dto : products) {
            Optional<Product> productOpt = productRepository.findById(dto.getId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                ProductDiscount discount = new ProductDiscount();
                discount.setProduct(product);
                discount.setDiscountPercent(12);
                discount.setActive(true);
                discount.setName("Giảm giá danh mục bán chậm");
                discount.setStartTime(LocalDateTime.now());
                discount.setEndTime(LocalDateTime.now().plusDays(7)); // Giảm trong 7 ngày

                discountRepository.save(discount);
            }
        }

    }

    @Override
    public int getTotalDiscountsCount() {
        return discountRepository.findAll().size();
    }

    @Override
    public List<ProductDiscount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public Page<ProductDiscount> getAllDiscounts(Pageable pageable) {
        return discountRepository.findAll(pageable);
    }

    @Override
    public Optional<ProductDiscount> getDiscountById(Long id) {
        return discountRepository.findById(id);
    }

    @Override
    public void updateDiscount(Long id, ProductDiscount discountDetails) {
        discountRepository.findById(id).map(productDiscount -> {
            productDiscount.setCategory(discountDetails.getCategory());
            productDiscount.setProduct(discountDetails.getProduct());
            productDiscount.setDiscountPercent(discountDetails.getDiscountPercent());
            productDiscount.setEndTime(discountDetails.getEndTime());
            productDiscount.setStartTime(discountDetails.getStartTime());
            return discountRepository.save(productDiscount);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy discount có ID: "+id));
    }

    @Override
    public Optional<ProductDiscount> getActiveDiscountForCategory(Boolean active, String name) {
        // Truy vấn từ database hoặc cache để lấy giảm giá cho danh mục
        return discountRepository.findProductDiscountByActiveAndCategory_Name(active,name);
    }

    @Override
    public void applyHolidayDiscount(LocalDate date, String discountName) {
        List<Product> allProducts = productService.getAllProducts();

        for (Product product : allProducts) {
            Optional<Product> productOpt = productRepository.findById(product.getId());
            if (productOpt.isPresent()) {
                ProductDiscount discount = new ProductDiscount();
                discount.setProduct(productOpt.get());
                discount.setDiscountPercent(20);
                discount.setActive(true);
                discount.setName(discountName);
                discount.setStartTime(date.atStartOfDay());
                discount.setEndTime(date.atStartOfDay().plusDays(3)); // Áp dụng trong 3 ngày

                discountRepository.save(discount);
            }
        }
    }


    @Override
    public void applySpecialDayDiscount(LocalDate date) {
        List<Product> specialProducts = productService.getAllProducts();

        for (Product product : specialProducts) {
            Optional<Product> productOpt = productRepository.findById(product.getId());
            if (productOpt.isPresent()) {
                ProductDiscount discount = new ProductDiscount();
                discount.setProduct(productOpt.get());
                discount.setDiscountPercent(15);
                discount.setActive(true);
                discount.setName("Ưu đãi đặc biệt ngày " + date.getDayOfMonth() + "/" + date.getMonthValue());
                discount.setStartTime(LocalDateTime.now());
                discount.setEndTime(LocalDateTime.now().plusDays(1)); // Giảm trong 1 ngày

                discountRepository.save(discount);
            }
        }
    }

}
