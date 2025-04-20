package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.fashion_web.backend.dto.DistrictDto;
import org.example.fashion_web.backend.dto.OrderDto;
import org.example.fashion_web.backend.dto.UserProfileDto;
import org.example.fashion_web.backend.dto.WardDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.CartItemService;
import org.example.fashion_web.backend.services.ImageService;
import org.example.fashion_web.backend.services.UserProfileService;
import org.example.fashion_web.backend.services.VoucherService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class OrderController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    private BigDecimal totalOrderPrice = BigDecimal.valueOf(0);

    private  String address;

    @GetMapping("/user/order")
    public String orderPage(Model model, HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetail) {
        if (userDetail == null) {
            return "redirect:/login"; // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
        }

        User user = userRepository.findById(userDetail.getUser().getId()).orElseThrow();

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileById(user.getId());
        UserProfile userProfile = userProfileOpt.orElse(new UserProfile()); // Tránh lỗi nếu user chưa có profile

        // Đưa dữ liệu vào model để Thymeleaf sử dụng
        model.addAttribute("user", new CustomUserDetails(user));

        // Tạo DTO để sử dụng trong form chỉnh sửa hồ sơ
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setUser(user);
        userProfileDto.setId(user.getId());
        userProfileDto.setDob(userProfile.getDob());
        userProfileDto.setAvatar(userProfile.getAvatar());
        userProfileDto.setPhoneNumber(userProfile.getPhoneNumber());
        userProfileDto.setWard(userProfile.getWard());

        userProfileDto.setAddress(userProfile.getAddress());

        model.addAttribute("userProfileDto", userProfileDto);

        // Lấy danh sách các thành phố để hiển thị trong combobox
        List<City> cities = cityRepository.findAll();

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

        address = (userProfile.getWard() != null)
        ?  userProfile.getAddress() + " Phường " + userProfile.getWard().getWardName() + ", Quận " +
        userProfile.getWard().getDistrict().getDistrictName() + ", Thành phố " +
        userProfile.getWard().getDistrict().getCity().getCityName()
        : "Chưa cập nhật!";

        List<Voucher> vouchers = voucherService.getAllVouchersAvilable(user.getId()); // Lấy danh sách voucher từ service

        model.addAttribute("detailaddress", address);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("totalOrderPrice", totalOrderPrice);
        model.addAttribute("productImages", productImages);
        model.addAttribute("cartItems", cart);
        System.out.println("Items cart sau khi load trang cart: " + cart);

        model.addAttribute("cities", cities);

        return "order/order";
    }

    // Lấy danh sách Quận/Huyện theo Thành phố
    @GetMapping("/order/districts")
    public ResponseEntity<List<DistrictDto>> getDistrictsByCity(@RequestParam Long cityId) {
        List<District> districts = districtRepository.findByCityId(cityId);
        List<DistrictDto> districtDtos = districts.stream().map(DistrictDto::new).toList();
        return ResponseEntity.ok(districtDtos);
    }


    @GetMapping("/order/wards")
    public ResponseEntity<List<WardDto>> getWardsByDistrict(@RequestParam Long districtId) {
        List<Ward> wards = wardRepository.findByDistrictId(districtId);
        List<WardDto> wardDtos = wards.stream().map(WardDto::new).toList();
        return ResponseEntity.ok(wardDtos);
    }

    @PostMapping("/apply-order-voucher")
    public ResponseEntity<?> applyVoucher(@RequestParam String voucherCode) {
        BigDecimal priceWithVoucher = totalOrderPrice;
        BigDecimal discountAmount = BigDecimal.valueOf(0);
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode);
        voucher.setUsageLimit(voucher.getUsageLimit()-1);
        voucherRepository.save(voucher);
        if (voucher.getDiscountType().equals("percentage")) {
            BigDecimal discountRate = voucher.getDiscountValue().divide(BigDecimal.valueOf(100));
            discountAmount = priceWithVoucher.multiply(discountRate);
            priceWithVoucher = priceWithVoucher.subtract(discountAmount);
        } else if (voucher.getDiscountType().equals("fixed")) {
            discountAmount = voucher.getDiscountValue();
            priceWithVoucher = priceWithVoucher.subtract(discountAmount);
        }

        if (priceWithVoucher.compareTo(BigDecimal.valueOf(0)) < 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false));
        }

        return ResponseEntity.ok(Map.of("success", true, "newTotal", priceWithVoucher,
                "discountAmount", discountAmount));
    }

    // lưu order vào session
    @PostMapping("payment/save-info")
    public ResponseEntity<Void> savePaymentInfo(@RequestBody OrderDto request, HttpSession session) {
        session.setAttribute("paymentInfo", request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("payment/get-info")
    public ResponseEntity<OrderDto> getPaymentInfo(HttpSession session) {
        OrderDto paymentInfo = (OrderDto) session.getAttribute("paymentInfo");
        if (paymentInfo == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(paymentInfo);
    }

    @PostMapping("/user/order/checkout")
    public String checkoutOrder(HttpSession session, Model model, @AuthenticationPrincipal CustomUserDetails userDetail) {
        try {
            OrderDto paymentInfo = (OrderDto) session.getAttribute("paymentInfo");
            List<CartItems> cartItems = (List<CartItems>) session.getAttribute("cartItems");
            for (CartItems item : cartItems) {
                System.out.println(item.toString()); // in ra toString()
            }
            // Add validation
            if (paymentInfo == null || userDetail == null || cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("errorMessage", "Invalid order information");
                return "redirect:/user/order";
            }


            // Validate order total
            if (paymentInfo.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                model.addAttribute("errorMessage", "Invalid order total");
                return "redirect:/user/order";
            }

            User user = userRepository.findById(userDetail.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order newOrder = new Order();
            newOrder.setUser(user);
            newOrder.setOrderDate(LocalDate.now());
            newOrder.setTotalPrice(paymentInfo.getTotalPrice());
            newOrder.setShippingAddress(paymentInfo.getShippingAddress());
            newOrder.setPaymentMethod(paymentInfo.getPaymentMethod());
            if (paymentInfo.getPaymentMethod().equals("CASH")) {
                newOrder.setStatus(Order.OrderStatusType.PENDING);
                orderRepository.save(newOrder);
                //lưu payment vào database
                Payment payment = new Payment();
                payment.setOrder(newOrder);
                payment.setPaymentMethod(Payment.PaymentMethodType.valueOf(paymentInfo.getPaymentMethod()));
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentStatus(String.valueOf(0));
                paymentRepository.save(payment);
            } else if (paymentInfo.getPaymentMethod().equals("BANK_TRANSFER")) {
                newOrder.setStatus(Order.OrderStatusType.PAYING);
                // lưu orderitem vào database
                for (CartItems item : cartItems) {
                    OrderItem orderItem = new OrderItem(newOrder, item.getProduct(), item.getQuantity(), item.getPricePerUnit());
                    Optional<Product> product = productRepository.findById(item.getProduct().getId());
                    productRepository.save(product.get());
                    orderItemRepository.save(orderItem);
                }
                orderRepository.save(newOrder);
                session.setAttribute("cartItems", null); // clear cart
                session.setAttribute("paymentOrderId", newOrder.getId()); // lưu orderId
                return "redirect:/user/order/payment";
            }


            // lưu orderitem vào database
            for (CartItems item : cartItems) {
                OrderItem orderItem = new OrderItem(newOrder, item.getProduct(), item.getQuantity(), item.getPricePerUnit());
                Optional<Product> product = productRepository.findById(item.getProduct().getId());
                product.get().setStock_quantity(product.get().getStock_quantity()-item.getQuantity());
                productRepository.save(product.get());
                orderItemRepository.save(orderItem);
            }

            //lưu voucher user vào database
            if (!paymentInfo.getVoucherCode().isEmpty()) {
                UserVoucher userVoucher = new UserVoucher();
                userVoucher.setUser(user);
                userVoucher.setOrder(newOrder);
                Voucher voucher = voucherRepository.findByVoucherCode(paymentInfo.getVoucherCode());
                voucher.setUsageLimit(voucher.getUsageLimit() - 1);
                voucherRepository.save(voucher);
                userVoucher.setVoucher(voucher);
                userVoucher.setUsedDate(LocalDateTime.now());
                userVoucherRepository.save(userVoucher);
            }

            if (paymentInfo.getPaymentMethod().equals("CASH")) {
                session.removeAttribute("cartItems");
                session.removeAttribute("paymentInfo");
                return "order/order-confirmination";
            } else {
                return "/order/order-payment";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Order processing failed: " + e.getMessage());
            System.out.println(e.getMessage());
            return "redirect:/user/order";
        }
    }

    //lưu user
    @PostMapping("/user/order/save-user")
    public String editProfile(@ModelAttribute("userProfileDto") UserProfileDto userProfileDto,
                              @RequestParam("cityId") Long cityId,
                              @RequestParam("districtId") Long districtId,
                              @RequestParam("wardId") Long wardId,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // Kiểm tra xem user có tồn tại không
        Optional<User> userOpt = userRepository.findById(userProfileDto.getId());
        System.out.println("City ID: " + cityId);
        System.out.println("District ID: " + districtId);
        System.out.println("Ward ID: " + wardId);
        if (userOpt.isEmpty()) {
            result.addError(new FieldError("userProfileDto", "user", "Người dùng không tồn tại!"));
            return "profile/profile";
        }
        User user = userOpt.get();
        System.out.println(user.toString());

        // Lấy hoặc tạo mới UserProfile
        UserProfile userProfile = userProfileRepository.findById(user.getId()).orElse(new UserProfile());

        user.setName(userProfileDto.getUser().getName());
        userProfile.setUser(user);
        userProfile.setDob(userProfileDto.getDob());
        userProfile.setPhoneNumber(userProfileDto.getPhoneNumber());
        userProfile.setAddress(userProfileDto.getAddress());

        // Xử lý Ward (kiểm tra null trước khi truy xuất thuộc tính con)
        Optional<Ward> wardOpt = wardRepository.findById(wardId);

        if (wardOpt.isPresent()) {
            wardOpt.ifPresent(userProfile::setWard);
            userProfile.setWard(wardOpt.get());
            Optional<District> districtOpt = districtRepository.findById(districtId);
            userProfile.getWard().setDistrict(districtOpt.get());
            Optional<City> cityOpt = cityRepository.findById(cityId);
            userProfile.getWard().getDistrict().setCity(cityOpt.get());
        }

        try {
            userRepository.save(user);
            userProfileRepository.save(userProfile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/user/order";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật hồ sơ! Vui lòng thử lại.");
            return "redirect:/user/order";
        }
    }
}
