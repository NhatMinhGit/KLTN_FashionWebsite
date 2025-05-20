package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.*;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.UserProfileService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ProfileController {
    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private CityRepository cityRepository;

    @GetMapping("/user/profile")
    public String profilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetail) {
        if (userDetail == null) {
            return "redirect:/login"; // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
        }

        User user = userRepository.findById(userDetail.getUser().getId()).orElseThrow();

        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileById(user.getId());
        UserProfile userProfile = userProfileOpt.orElse(new UserProfile()); // Tránh lỗi nếu user chưa có profile

        // Đưa dữ liệu vào model để Thymeleaf sử dụng
        model.addAttribute("currentPage", "profile");
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
        model.addAttribute("cities", cities);

        return "profile/profile"; // Trả về trang HTML profile
    }

    // Lấy danh sách Quận/Huyện theo Thành phố
    @GetMapping("/districts")
    public ResponseEntity<List<DistrictDto>> getDistrictsByCity(@RequestParam Long cityId) {
        List<District> districts = districtRepository.findByCityId(cityId);
        List<DistrictDto> districtDtos = districts.stream().map(DistrictDto::new).toList();
        return ResponseEntity.ok(districtDtos);
    }


    @GetMapping("/wards")
    public ResponseEntity<List<WardDto>> getWardsByDistrict(@RequestParam Long districtId) {
        List<Ward> wards = wardRepository.findByDistrictId(districtId);
        List<WardDto> wardDtos = wards.stream().map(WardDto::new).toList();
        return ResponseEntity.ok(wardDtos);
    }


    @PostMapping("/user/profile")
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

        //Cập nhật khi có file mới
        if (userProfileDto.getAvatar() != null) {
            userProfile.setAvatar(userProfileDto.getAvatar());
        }

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

//        // Định dạng địa chỉ đầy đủ
//        String address = (userProfile.getWard() != null)
//                ? "Phường " + userProfile.getWard().getWardName() + ", Quận " +
//                userProfile.getWard().getDistrict().getDistrictName() + ", Thành phố " +
//                userProfile.getWard().getDistrict().getCity().getCityName()
//                : "Chưa cập nhật!";
//        System.out.println(address);

        try {
            userRepository.save(user);
            userProfileRepository.save(userProfile);
            redirectAttributes.addFlashAttribute("swalTitle", "Cập nhật thành công");
            redirectAttributes.addFlashAttribute("swalMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/user/profile";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật hồ sơ! Vui lòng thử lại.");
            return "redirect:/user/profile";
        }
    }
}
