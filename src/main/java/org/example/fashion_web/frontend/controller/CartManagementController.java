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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public String showCart(Model model, HttpSession session, Principal principal) {
        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
        System.out.println(cart);
        if (cart == null) {
            cart = new ArrayList<>();
        }

        // Nhóm danh sách ảnh theo productId
        Map<Long, List<String>> productImages = new HashMap<>();
        for (CartItems item : cart) {
            List<Image> images = imageService.findImagesByProductId(item.getProduct().getId());
            List<String> imageUrls = images.stream().map(Image::getImageUri).collect(Collectors.toList());
            productImages.put(item.getProduct().getId(), imageUrls);

        }
        totalOrderPrice = cartItemService.getTotalPrice(cart);
        model.addAttribute("totalOrderPrice", cartItemService.getTotalPrice(cart));
        model.addAttribute("productImages", productImages);
        model.addAttribute("cartItems", cart);
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
            if (!requestData.containsKey("productId") || !requestData.containsKey("quantity")) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin sản phẩm hoặc số lượng!");
                return ResponseEntity.badRequest().body(response);
            }

            Long productId = Long.parseLong(requestData.get("productId").toString());
            int quantity = Integer.parseInt(requestData.get("quantity").toString());

//            if (quantity <= 0) {
//                response.put("success", false);
//                response.put("message", "Số lượng phải lớn hơn 0!");
//                return ResponseEntity.badRequest().body(response);
//            }

            if (!requestData.containsKey("size")) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin size!");
                return ResponseEntity.badRequest().body(response);
            }

            String sizeName = requestData.get("size").toString();

            // Kiểm tra sản phẩm có tồn tại không
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            Size size = sizeService.findByProductAndSizeName(product, sizeName)
                    .orElseThrow(() -> new RuntimeException("Size không tồn tại cho sản phẩm này!"));

            if (size.getStockQuantity() < quantity) {
                response.put("success", false);
                response.put("message", "Không đủ hàng trong kho cho size đã chọn!");
                return ResponseEntity.badRequest().body(response);
            }

            BigDecimal effectivePrice = discountService.getActiveDiscountForProduct(product)
                    .map(discount -> discountService.applyDiscount(product.getPrice(), discount))
                    .orElse(product.getPrice());
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
                System.out.println("Cart ID trong Cart: " + cart.getCartId());
                cart = cartService.save(cart); // Lưu vào DB
                System.out.println("Cart ID sau khi flush: " + cart.getCartId());
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
                            item.getSize().getSizeName().equals(sizeName)) // thêm điều kiện này
                    .findFirst();


            if (existingItem.isPresent()) {
                CartItems item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                cartItemService.save(item); // Cập nhật DB

            } else {
                CartItems newItem = new CartItems();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);

                newItem.setPricePerUnit(product.getEffectivePrice());
                System.out.println("Cart ID trong CartItems: " + newItem.getCart());
                cartItemService.save(newItem); // Lưu vào DB
                cartItems.add(newItem);
            }
            // Trừ tồn kho
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


//    @PostMapping("/user/cart/remove-from-cart")
//    public String removeFromCart(@RequestParam("productId") Long productId, HttpSession session) {
//        List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");
//        if (cart != null) {
//            cart.removeIf(item -> item.getProduct().getId().equals(productId));
//            session.setAttribute("cartItems", cart);
//        }
//        return "redirect:/user/cart";
//    }
@PostMapping("user/cart/remove-from-cart")
public String removeFromCart(@RequestParam("id") Long cartItemId, HttpSession session) {
    List<CartItems> cart = (List<CartItems>) session.getAttribute("cartItems");

    if (cart == null) {
        System.out.println("Cart is NULL! No items to remove.");
        return "redirect:/user/cart";
    }

    System.out.println("Before removal: " + cart);
    System.out.println("Attempting to remove item with ID: " + cartItemId);
    boolean removed = cart.removeIf(item -> Objects.equals(item.getCartItemId(), cartItemId));
    System.out.println("After removal: " + cart);
    System.out.println("Item removed: " + removed);


    if (removed) {
        System.out.println("Item removed successfully!");
    } else {
        System.out.println("Item not found in the cart!");
    }


    session.setAttribute("cartItems", cart);

    return "redirect:/user/cart";
}












}
