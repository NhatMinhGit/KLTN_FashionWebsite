package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class CartManagementController {
    @Autowired
    private final ProductService productService; // Service để lấy thông tin sản phẩm từ DB
    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService
    @Autowired
    private UserService userService; // Inject UserDetailsService
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private SizeService sizeService;

    private BigDecimal totalOrderPrice = BigDecimal.valueOf(0);



    public CartManagementController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping("/user/cart")
    public String showCart(Model model, HttpSession session) {
        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
        System.out.println(cart);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (CartItems item : cart) {
            List<Image> images = imageService.findImagesByProductVariantId(item.getProduct().getId());
            List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
            productImages.put(item.getProduct().getId(), imageUrls);

        }
        totalOrderPrice = cartItemService.getTotalPrice(cart);

        model.addAttribute("totalOrderPrice", cartItemService.getTotalPrice(cart));
        model.addAttribute("productImages", productImages);
        model.addAttribute("cartItems", cart);
        model.addAttribute("countCart", cart.size());

        System.out.println("Items cart sau khi load trang cart: " + cart);
        return "cart/cart";
    }

    @PostMapping("/user/cart/add-to-cart")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, Object> requestData,
            HttpSession session,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra dữ liệu đầu vào
            if (!requestData.containsKey("productId") || !requestData.containsKey("quantity") || !requestData.containsKey("variantId") || !requestData.containsKey("size")) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin sản phẩm, size hoặc variant!");
                return ResponseEntity.badRequest().body(response);
            }

            Long productId = Long.parseLong(requestData.get("productId").toString());
            int quantity = Integer.parseInt(requestData.get("quantity").toString());
            Long variantId = Long.parseLong(requestData.get("variantId").toString());
            String sizeName = requestData.get("size").toString();

            // Kiểm tra sản phẩm có tồn tại không
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            // Tìm variant tương ứng với sản phẩm
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Variant không tồn tại cho sản phẩm này!"));

            // Lấy danh sách size từ sizeService thay vì từ variant
            List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());

            // Tìm size tương ứng với sizeName
            Size size = sizes.stream()
                    .filter(s -> s.getSizeName().equals(sizeName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Size không tồn tại cho variant này!"));

            // Kiểm tra tồn kho size
            if (size.getStockQuantity() < quantity) {
                response.put("success", false);
                response.put("message", "Không đủ hàng trong kho cho size đã chọn!");
                return ResponseEntity.badRequest().body(response);
            }

            // Áp dụng giảm giá nếu có
            List<ProductDiscount> productDiscounts = discountService.getActiveDiscountsForProduct(product);

            // Lấy giảm giá cho danh mục
            List<ProductDiscount> categoryDiscounts = discountService.getActiveDiscountsForCategory(product.getCategory());

            // Gộp cả 2 danh sách giảm giá
            Stream<ProductDiscount> allDiscounts = Stream.concat(
                    productDiscounts.stream(),
                    categoryDiscounts.stream()
            );

            // Tìm giảm giá cao nhất
            Optional<ProductDiscount> maxDiscount = allDiscounts
                    .max(Comparator.comparing(ProductDiscount::getDiscountPercent));

            // Áp dụng giảm giá cao nhất (nếu có)
            BigDecimal effectivePrice = maxDiscount
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());

            // Cập nhật giá hiệu lực cho sản phẩm
            product.setEffectivePrice(effectivePrice);

            // Lấy giỏ hàng từ session hoặc tạo mới nếu chưa có
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                String username = principal.getName();
                User user = userService.findByEmail(username);

                if (user == null) {
                    response.put("success", false);
                    response.put("message", "Người dùng không hợp lệ!");
                    return ResponseEntity.badRequest().body(response);
                }

                cart = new Cart();
                cart.setUser(user);
                cart.setCreatedAt(LocalDateTime.now());
                cart = cartService.save(cart); // Lưu vào DB
                session.setAttribute("cart", cart);
            }

            // Lấy danh sách CartItems
            List<CartItems> cartItems = (List<CartItems>) session.getAttribute("cartItems");
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
            Optional<CartItems> existingItem = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId) &&
                            item.getSize().getSizeName().equals(sizeName) &&
                            item.getVariant().getId().equals(variantId)) // Thêm kiểm tra variantId
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItems item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartItemService.save(item); // Cập nhật DB
            } else {
                CartItems newItem = new CartItems();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setVariant(variant); // Gán variant vào CartItems
                newItem.setSize(size); // Gán size vào CartItems
                newItem.setQuantity(quantity);
                newItem.setPricePerUnit(product.getEffectivePrice());

                cartItemService.save(newItem); // Lưu vào DB
                cartItems.add(newItem);
            }

            // Trừ tồn kho size
            size.setStockQuantity(size.getStockQuantity() - quantity);
            sizeService.save(size);

            // Cập nhật session
            session.setAttribute("cartItems", cartItems);

            response.put("success", true);
            response.put("message", "Sản phẩm đã được thêm vào giỏ hàng!");
            response.put("cartSize", cartItems.size());
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Lỗi định dạng dữ liệu! Vui lòng nhập đúng kiểu số.");
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PostMapping("user/cart/remove-from-cart")
    public String removeFromCart(@RequestParam("id") Integer cartItemId,
                                 HttpSession session,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");

        Optional<CartItems> item = cartItemService.findById(cartItemId);
        if (item != null) {
            Size size = item.get().getSize();
            int quantityToReturn = item.get().getQuantity();

            size.setStockQuantity(size.getStockQuantity() + quantityToReturn);
            sizeService.save(size);

            cartItemService.removeCartItem(cartItemId);
        }
        if (cart == null) {
            System.out.println("Cart is NULL! No items to remove.");
            return "redirect:" + (referer != null ? referer : "/user/cart");
        }

        if (cart != null) {
            cart.removeIf(ci -> Objects.equals(ci.getCartItemId(), cartItemId));
            session.setAttribute("cartItems", cart);
        }

        System.out.println("Redirecting to: " + referer);

        return "redirect:" + (referer != null ? referer : "/user/cart");
    }

    @PostMapping("/user/cart/update-quantity")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestParam("id") Integer cartItemId,
                                            @RequestParam("change") Integer change,
                                            HttpSession session) {
        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
        if (cart == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart not found in session."));
        }

        for (CartItems item : cart) {
            if (Objects.equals(item.getCartItemId(), cartItemId)) {
                int newQuantity = item.getQuantity() + change;
                if (newQuantity < 1) newQuantity = 1;
                item.setQuantity(newQuantity);

                cartItemService.updateCartItemQuantity(cartItemId, newQuantity);

                session.setAttribute("cartItems", cart);
                return ResponseEntity.ok(Map.of("newQuantity", newQuantity));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Cart item not found."));
    }
}
