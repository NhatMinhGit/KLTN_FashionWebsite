package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.dto.DistrictDto;
import org.example.fashion_web.backend.dto.UserProfileDto;
import org.example.fashion_web.backend.dto.WardDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.CityRepository;
import org.example.fashion_web.backend.repositories.DistrictRepository;
import org.example.fashion_web.backend.repositories.WardRepository;
import org.example.fashion_web.backend.services.UserProfileService;
import org.example.fashion_web.backend.services.UserService;
import org.example.fashion_web.backend.services.servicesimpl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ProfileController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

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

        User user = userDetail.getUser();
        Optional<UserProfile> userProfileOpt = userProfileService.getUserProfileById(user.getId());
        UserProfile userProfile = userProfileOpt.orElse(new UserProfile()); // Tránh lỗi nếu user chưa có profile

        // Đưa dữ liệu vào model để Thymeleaf sử dụng
        model.addAttribute("currentPage", "profile");
        model.addAttribute("user", userDetail);
//        model.addAttribute("profile", userProfile);

        // Tạo DTO để sử dụng trong form chỉnh sửa hồ sơ
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setUser(user);
        userProfileDto.setDob(userProfile.getDob());
        userProfileDto.setAvatar(userProfile.getAvatar());
        userProfileDto.setPhoneNumber(userProfile.getPhoneNumber());
        userProfileDto.setWard(userProfile.getWard());
        userProfileDto.setId(userProfile.getId());
        userProfileDto.setAddress(userProfile.getAddress());

        model.addAttribute("userProfileDto", userProfileDto);

//        // Định dạng địa chỉ đầy đủ
//        String address = (userProfile.getWard() != null)
//                ? "Phường " + userProfile.getWard().getWardName() + ", Quận " +
//                userProfile.getWard().getDistrict().getDistrictName() + ", Thành phố " +
//                userProfile.getWard().getDistrict().getCity().getCityName()
//                : "Chưa cập nhật!";
//        model.addAttribute("address", address);

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

}
