package org.example.fashion_web.frontend.controller;

import jakarta.validation.Valid;
import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.Order;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    @Autowired
    private VoucherRepository voucherRepository;

//    @RequestMapping("admin/voucher")
//    public String voucherIndex(Model model, Principal principal) {
//
//        List<Voucher> vouchers = voucherRepository.findAll();
//
//        model.addAttribute("vouchers", vouchers);
//        if (principal != null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
//            if (userDetails != null) {
//                model.addAttribute("user", userDetails);
//            } else {
//                model.addAttribute("user", new User()); // Tránh lỗi NullPointerException
//            }
//        }
//        return "voucher/index";
//    }

    @GetMapping("/admin/voucher")
    public String listVouchersPaging(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "discountType", required = false) String discountType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model, Principal principal) {

        Pageable pageable = PageRequest.of(page, size);

        List<Voucher> filtered = voucherService.searchVouchersByKeyword(keyword);
        List<Voucher> original = filtered;

        // Lọc theo discountType
        if (discountType != null && (discountType.equals("percentage") || discountType.equals("fixed"))) {
            filtered = filtered.stream()
                    .filter(v -> v.getDiscountType().equals(discountType))
                    .collect(Collectors.toList());
        }

        // Xử lý status
        if (status != null) {
            status = status.trim().toLowerCase();
            // Chỉ chấp nhận giá trị hợp lệ
            if (!status.equals("active") && !status.equals("inactive")) {
                status = null; // Bỏ qua nếu status không hợp lệ
            }
        }

        if ("active".equals(status)) {
            filtered = filtered.stream()
                    .filter(v -> !v.getStartDate().isAfter(LocalDate.now()) &&
                            !v.getEndDate().isBefore(LocalDate.now()) &&
                            v.getUsageLimit() > 0)
                    .collect(Collectors.toList());
            if (filtered.size() == original.size() && filtered.containsAll(original)) {
                filtered = new ArrayList<>();
                model.addAttribute("filterMessage", "Voucher với các tiêu chí tìm kiếm đang hoạt động");
            }
        } else if ("inactive".equals(status)) {
            filtered = filtered.stream()
                    .filter(v -> v.getEndDate().isBefore(LocalDate.now()) ||
                            v.getUsageLimit() <= 0)
                    .collect(Collectors.toList());
            if (filtered.size() == original.size() && filtered.containsAll(original)) {
                filtered = new ArrayList<>();
                model.addAttribute("filterMessage", "Voucher với các tiêu chí tìm kiếm đang không hoạt động.");
            }
        }

        // Phân trang
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        Page<Voucher> voucherPage = new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());

        // Thêm các thuộc tính vào model
        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("totalPages", voucherPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        model.addAttribute("param", Map.of(
                "keyword", keyword != null ? keyword : "",
                "status", status != null ? status : "",
                "discountType", discountType != null ? discountType : ""
        ));

        // Xử lý thông tin người dùng
        if (principal != null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
                model.addAttribute("user", userDetails);
            } catch (Exception e) {
                model.addAttribute("user", new User());
            }
        }

        return "voucher/vouchers-paging";
    }


    @GetMapping("admin/voucher/add-voucher")
    public String createUser (Model model) {
        UserDto userDto = new UserDto();
        VoucherDto voucherDto = new VoucherDto();
        model.addAttribute("voucherDto", voucherDto);
        return "voucher/add-voucher";
    }

    @PostMapping("admin/voucher/add-voucher")
    public String createVoucher(@Valid @ModelAttribute("voucherDto") VoucherDto voucherDto,
                                BindingResult result, Model model) {
        // Kiểm tra xem mã voucher đã tồn tại chưa
        if (voucherRepository.findByVoucherCode(voucherDto.getVoucherCode()) != null) {
            result.addError(new FieldError("voucherDto", "voucherCode", "Mã voucher đã tồn tại!"));
        }

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            model.addAttribute("voucherDto", voucherDto); // Đảm bảo dữ liệu không bị mất khi có lỗi
            return "voucher/add-voucher";
        }

        // Chuyển đổi DTO thành entity và lưu vào database
        Voucher voucher = new Voucher();
        voucher.setVoucherCode(voucherDto.getVoucherCode());
        voucher.setVoucherName(voucherDto.getVoucherName());
        voucher.setDiscountType(voucherDto.getDiscountType());
        voucher.setDiscountValue(voucherDto.getDiscountValue());
        voucher.setMinOrderValue(voucherDto.getMinOrderValue());
        voucher.setStartDate(LocalDate.from(voucherDto.getStartDate()));
        voucher.setEndDate(LocalDate.from(voucherDto.getEndDate()));
        voucher.setUsageLimit(voucherDto.getUsageLimit());
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);

        return "redirect:/admin/voucher";
    }

    @GetMapping("admin/voucher/edit-voucher")
    public String editVoucher(Model model, @RequestParam Long id) {
        Voucher voucher = voucherRepository.findById(id).orElse(null);
        if (voucher == null) {
            return "redirect:/admin/voucher";
        }

        VoucherDto voucherDto = new VoucherDto();
        voucherDto.setId(voucher.getId());
        voucherDto.setVoucherCode(voucher.getVoucherCode());
        voucherDto.setVoucherName(voucher.getVoucherName());
        voucherDto.setDiscountType(voucher.getDiscountType());
        voucherDto.setDiscountValue(voucher.getDiscountValue());
        voucherDto.setMinOrderValue(voucher.getMinOrderValue());
        voucherDto.setStartDate(voucher.getStartDate());
        voucherDto.setEndDate(voucher.getEndDate());
        voucherDto.setUsageLimit(voucher.getUsageLimit());

        model.addAttribute("voucherDto", voucherDto);

        return "voucher/edit-voucher";
    }

    @PostMapping("admin/voucher/edit-voucher")
    public String editVoucher(
            Model model,
            @RequestParam Long id,
            @Valid @ModelAttribute("voucherDto") VoucherDto voucherDto,
            BindingResult result) {

        Voucher voucher = voucherRepository.findById(id).orElse(null);
        if (voucher == null) {
            return "redirect:/admin/voucher";
        }

        if (!voucher.getVoucherCode().equals(voucherDto.getVoucherCode()) &&
                voucherRepository.findByVoucherCode(voucherDto.getVoucherCode()) != null) {
            result.addError(new FieldError("voucherDto", "voucherCode", "Mã voucher đã tồn tại!"));
        }

        if (result.hasErrors()) {
            model.addAttribute("voucherDto", voucherDto);
            return "voucher/edit-voucher";
        }

        voucher.setVoucherCode(voucherDto.getVoucherCode());
        voucher.setVoucherName(voucherDto.getVoucherName());
        voucher.setDiscountType(voucherDto.getDiscountType());
        voucher.setDiscountValue(voucherDto.getDiscountValue());
        voucher.setMinOrderValue(voucherDto.getMinOrderValue());
        voucher.setStartDate(voucherDto.getStartDate());
        voucher.setEndDate(voucherDto.getEndDate());
        voucher.setUsageLimit(voucherDto.getUsageLimit());
        voucher.setCreatedAt(voucherDto.getCreatedAt());
        voucher.setUpdatedAt(LocalDateTime.now());

        voucherRepository.save(voucher);

        return "redirect:/admin/voucher";
    }

    @GetMapping("admin/voucher/delete-voucher")
    public String deleteVoucher(@RequestParam Long id) {
        Voucher voucher = voucherRepository.findById(id).orElse(null);

        if (voucher != null) {
            voucherRepository.delete(voucher);
        }

        return "redirect:/admin/voucher";
    }

}
