package org.example.fashion_web.frontend.controller;

import jakarta.validation.Valid;
import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.dto.UserVoucherAssignmentDto;
import org.example.fashion_web.backend.dto.VoucherDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.models.UserVoucherAssignment;
import org.example.fashion_web.backend.models.Voucher;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.repositories.UserVoucherAssignmentRepository;
import org.example.fashion_web.backend.repositories.VoucherRepository;
import org.example.fashion_web.backend.services.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserVoucherAssignmentRepository userVoucherAssignmentRepository;

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

        // Map voucherId -> List tên user assigned (nếu có)
        Map<Long, List<String>> voucherAssignments = new HashMap<>();
        for (Voucher v : voucherPage.getContent()) {
            List<UserVoucherAssignment> assignments = userVoucherAssignmentRepository.getAssignmentsByVoucherId(v.getId());
            if (assignments != null && !assignments.isEmpty()) {
                List<String> userNames = assignments.stream()
                        .map(a -> a.getUser().getName()) // giả sử User có method getName()
                        .collect(Collectors.toList());
                voucherAssignments.put(v.getId(), userNames);
            } else {
                // Nếu không có assignment thì có thể để rỗng hoặc null
                voucherAssignments.put(v.getId(), Collections.emptyList());
            }
        }
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

        model.addAttribute("voucherAssignments", voucherAssignments);

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
        VoucherDto voucherDto = new VoucherDto();
        model.addAttribute("voucherDto", voucherDto);
        List<User> users = userService.getUsersByRole("USER"); // hoặc lấy user phù hợp
        model.addAttribute("users", users);
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
        model.addAttribute("voucherDto", voucherDto);
        return "voucher/add-voucher";
    }

    // Tạo entity Voucher và lưu
    Voucher voucher = new Voucher();
    voucher.setVoucherCode(voucherDto.getVoucherCode());
    voucher.setVoucherName(voucherDto.getVoucherName());
    voucher.setDiscountType(voucherDto.getDiscountType());
    voucher.setDiscountValue(voucherDto.getDiscountValue());
    voucher.setMinOrderValue(voucherDto.getMinOrderValue());
    voucher.setStartDate(voucherDto.getStartDate());
    voucher.setEndDate(voucherDto.getEndDate());
    voucher.setUsageLimit(voucherDto.getUsageLimit());
    voucher.setCreatedAt(LocalDateTime.now());
    voucher.setUpdatedAt(LocalDateTime.now());
    voucherRepository.save(voucher);


    // Nếu có userId (khác null và khác rỗng), tạo thêm UserVoucherAssignment
    if (voucherDto.getUserId() != null) {
        Optional<User> userOpt = userRepository.findById(voucherDto.getUserId());
        if (userOpt.isPresent()) {
            UserVoucherAssignment assignment = new UserVoucherAssignment();
            assignment.setUser(userOpt.get());
            assignment.setVoucher(voucher);
            assignment.setAssignedAt(LocalDateTime.now());
            assignment.setIsUsed(false);
            userVoucherAssignmentRepository.save(assignment);
        }
    }

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

        // Lấy danh sách user để dropdown chọn người dùng áp dụng
        List<User> users = userService.getUsersByRole("USER");
        model.addAttribute("users", users);

        // Tìm userId đã được gán voucher này (nếu có)
        List<UserVoucherAssignment> assignments = userVoucherAssignmentRepository.getAssignmentsByVoucherId(voucher.getId());
        if (assignments != null && !assignments.isEmpty()) {
            voucherDto.setUserId(assignments.get(0).getUser().getId());
        }

        model.addAttribute("voucherDto", voucherDto);

        return "voucher/edit-voucher";
    }

//    @PostMapping("admin/voucher/edit-voucher")
//    public String editVoucher(
//            Model model,
//            @RequestParam Long id,
//            @Valid @ModelAttribute("voucherDto") VoucherDto voucherDto,
//            BindingResult result) {
//
//        Voucher voucher = voucherRepository.findById(id).orElse(null);
//        if (voucher == null) {
//            return "redirect:/admin/voucher";
//        }
//
//        if (!voucher.getVoucherCode().equals(voucherDto.getVoucherCode()) &&
//                voucherRepository.findByVoucherCode(voucherDto.getVoucherCode()) != null) {
//            result.addError(new FieldError("voucherDto", "voucherCode", "Mã voucher đã tồn tại!"));
//        }
//
//        if (result.hasErrors()) {
//            List<User> users = userService.getUsersByRole("USER");
//            model.addAttribute("users", users);
//            model.addAttribute("voucherDto", voucherDto);
//            return "voucher/edit-voucher";
//        }
//
//        // Cập nhật các trường voucher
//        voucher.setVoucherCode(voucherDto.getVoucherCode());
//        voucher.setVoucherName(voucherDto.getVoucherName());
//        voucher.setDiscountType(voucherDto.getDiscountType());
//        voucher.setDiscountValue(voucherDto.getDiscountValue());
//        voucher.setMinOrderValue(voucherDto.getMinOrderValue());
//        voucher.setStartDate(voucherDto.getStartDate());
//        voucher.setEndDate(voucherDto.getEndDate());
//        voucher.setUsageLimit(voucherDto.getUsageLimit());
//        voucher.setUpdatedAt(LocalDateTime.now());
//
//        voucherRepository.save(voucher);
//
//        // Xử lý user assignment
//        Long userId = voucherDto.getUserId();
//        List<UserVoucherAssignment> existingAssignmentOpt = userVoucherAssignmentRepository.getAssignmentsByVoucherId(voucher.getId());
//        if (userId != null) {
//            Optional<User> userOpt = userRepository.findById(userId);
//            if (userOpt.isPresent()) {
//                if (existingAssignmentOpt != null && !existingAssignmentOpt.isEmpty()) {
//                    // Cập nhật user trong assignment nếu khác
//                    UserVoucherAssignment existingAssignment = existingAssignmentOpt.get(0);
//                    if (!existingAssignment.getUser().getId().equals(userId)) {
//                        existingAssignment.setUser(userOpt.get());
//                        existingAssignment.setAssignedAt(LocalDateTime.now());
//                        existingAssignment.setIsUsed(false);
//                        userVoucherAssignmentRepository.save(existingAssignment);
//                    }
//                } else {
//                    // Tạo mới assignment nếu chưa có
//                    UserVoucherAssignment assignment = new UserVoucherAssignment();
//                    assignment.setUser(userOpt.get());
//                    assignment.setVoucher(voucher);
//                    assignment.setAssignedAt(LocalDateTime.now());
//                    assignment.setIsUsed(false);
//                    userVoucherAssignmentRepository.save(assignment);
//                }
//            }
//        } else {
//            if (existingAssignmentOpt != null && !existingAssignmentOpt.isEmpty()) {
//                for (UserVoucherAssignment assignment : existingAssignmentOpt) {
//                    userVoucherAssignmentRepository.delete(assignment);
//                }
//            }
//        }
//
//        return "redirect:/admin/voucher";
//    }
@PostMapping("admin/voucher/edit-voucher")
public String editVoucher(
        Model model,
        @RequestParam Long id,
        @Valid @ModelAttribute("voucherDto") VoucherDto voucherDto,
        BindingResult result) {

    // 1. Lấy thông tin voucher hiện tại từ DB
    Voucher existingVoucher = voucherRepository.findById(id).orElse(null);
    if (existingVoucher == null) {
        return "redirect:/admin/voucher";
    }

    // 2. Kiểm tra trùng mã voucher nếu mã được sửa
    boolean isCodeChanged = !existingVoucher.getVoucherCode().equals(voucherDto.getVoucherCode());
    boolean isCodeDuplicated = voucherRepository.findByVoucherCode(voucherDto.getVoucherCode()) != null;
    if (isCodeChanged && isCodeDuplicated) {
        result.addError(new FieldError("voucherDto", "voucherCode", "Mã voucher đã tồn tại!"));
    }

    // 3. Nếu có lỗi validate, trả về lại form chỉnh sửa
    if (result.hasErrors()) {
        model.addAttribute("users", userService.getUsersByRole("USER"));
        model.addAttribute("voucherDto", voucherDto);
        return "voucher/edit-voucher";
    }

    // 4. Cập nhật thông tin voucher
    existingVoucher.setVoucherCode(voucherDto.getVoucherCode());
    existingVoucher.setVoucherName(voucherDto.getVoucherName());
    existingVoucher.setDiscountType(voucherDto.getDiscountType());
    existingVoucher.setDiscountValue(voucherDto.getDiscountValue());
    existingVoucher.setMinOrderValue(voucherDto.getMinOrderValue());
    existingVoucher.setStartDate(voucherDto.getStartDate());
    existingVoucher.setEndDate(voucherDto.getEndDate());
    existingVoucher.setUsageLimit(voucherDto.getUsageLimit());
    existingVoucher.setUpdatedAt(LocalDateTime.now());
    voucherRepository.save(existingVoucher);

    // 5. Xử lý gán user (nếu có)
    Long selectedUserId = voucherDto.getUserId();
    List<UserVoucherAssignment> currentAssignments =
            userVoucherAssignmentRepository.getAssignmentsByVoucherId(existingVoucher.getId());

    // Nếu có chọn user
    if (selectedUserId != null) {
        Optional<User> selectedUserOpt = userRepository.findById(selectedUserId);

        if (selectedUserOpt.isPresent()) {
            User selectedUser = selectedUserOpt.get();

            if (!currentAssignments.isEmpty()) {
                // Nếu đã có assignment → kiểm tra và cập nhật nếu khác user
                UserVoucherAssignment currentAssignment = currentAssignments.get(0);

                boolean isDifferentUser = !currentAssignment.getUser().getId().equals(selectedUserId);
                if (isDifferentUser) {
                    currentAssignment.setUser(selectedUser);
                    currentAssignment.setAssignedAt(LocalDateTime.now());
                    currentAssignment.setIsUsed(false);
                    userVoucherAssignmentRepository.save(currentAssignment);
                }

            } else {
                // Nếu chưa có assignment → tạo mới
                UserVoucherAssignment newAssignment = new UserVoucherAssignment();
                newAssignment.setUser(selectedUser);
                newAssignment.setVoucher(existingVoucher);
                newAssignment.setAssignedAt(LocalDateTime.now());
                newAssignment.setIsUsed(false);
                userVoucherAssignmentRepository.save(newAssignment);
            }
        }

    } else {
        // Nếu không chọn user → xóa tất cả assignment hiện có
        if (!currentAssignments.isEmpty()) {
            for (UserVoucherAssignment assignment : currentAssignments) {
                userVoucherAssignmentRepository.delete(assignment);
            }
        }
    }

    // 6. Chuyển hướng về danh sách voucher sau khi cập nhật thành công
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

    @GetMapping("/admin/voucher/assign")
    public String showAssignVoucherForm(Model model) {
        // Lấy danh sách user & voucher để dropdown
        List<User> users = userService.getUsersByRole("USER");
        List<Voucher> vouchers = voucherService.getVoucherNotAssign();

        model.addAttribute("users", users);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("assignmentDto", new UserVoucherAssignmentDto());

        return "voucher/assign-voucher";
    }

    @PostMapping("/admin/voucher/assign")
    public String assignVoucherToUser(@Valid @ModelAttribute("assignmentDto") UserVoucherAssignmentDto assignmentDto,
                                      BindingResult result,
                                      Model model) {
        List<User> users = userService.getUsersByRole("USER");
        List<Voucher> vouchers = voucherService.getVoucherAvilable();
        if (result.hasErrors()) {
            // Nếu có lỗi validation, trả lại danh sách để render dropdown
            model.addAttribute("users", users);
            model.addAttribute("vouchers", vouchers);
            return "voucher/assign-voucher";
        }

        Optional<User> userOpt = userRepository.findById(assignmentDto.getUserId());
        Optional<Voucher> voucherOpt = voucherRepository.findById(assignmentDto.getVoucherId());

        if (userOpt.isEmpty()) {
            result.rejectValue("userId", "error.assignmentDto", "Người dùng không tồn tại.");
        }
        if (voucherOpt.isEmpty()) {
            result.rejectValue("voucherId", "error.assignmentDto", "Voucher không tồn tại.");
        }

        if (result.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("vouchers", voucherRepository.findAll());
            return "voucher/assign-voucher";
        }

        // Kiểm tra xem đã có assignment cho user và voucher chưa (unique constraint)
        boolean exists = userVoucherAssignmentRepository.existsByUserIdAndVoucherId(assignmentDto.getUserId(), assignmentDto.getVoucherId());
        if (exists) {
            result.rejectValue("voucherId", "error.assignmentDto", "Voucher này đã được gán cho người dùng.");
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("vouchers", voucherRepository.findAll());
            return "voucher/assign-voucher";
        }

        UserVoucherAssignment assignment = new UserVoucherAssignment();
        assignment.setUser(userOpt.get());
        assignment.setVoucher(voucherOpt.get());
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setIsUsed(false);

        userVoucherAssignmentRepository.save(assignment);

        return "redirect:/admin/voucher?success=assign";
    }

}
