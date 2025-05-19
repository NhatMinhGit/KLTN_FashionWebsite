package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Branch;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BranchController {

    @GetMapping("/branch")
    public String getBranchPage(Model model) {
        List<Branch> branches = new ArrayList<>();

        Branch b1 = new Branch();
        b1.setBranchName("Chi nhánh Quận 1");
        b1.setAddress("12 Nguyễn Huệ");
        b1.setWard("Phường Bến Nghé");
        b1.setDistrict("Quận 1");
        b1.setCity("TP.HCM");
        b1.setLatitude(10.7769);
        b1.setLongitude(106.7009);
        b1.setPhone("0123 456 789");

        Branch b2 = new Branch();
        b2.setBranchName("Chi nhánh Quận 3");
        b2.setAddress("45 Võ Văn Tần");
        b2.setWard("Phường 6");
        b2.setDistrict("Quận 3");
        b2.setCity("TP.HCM");
        b2.setLatitude(10.7798);
        b2.setLongitude(115.6820);
        b2.setPhone("0987 654 321");

        Branch b3 = new Branch();
        b3.setBranchName("Chi nhánh Quận 5");
        b3.setAddress("102 Trần Hưng Đạo");
        b3.setWard("Phường 7");
        b3.setDistrict("Quận 5");
        b3.setCity("TP.HCM");
        b3.setLatitude(10.7565);
        b3.setLongitude(105.6657);
        b3.setPhone("0909 888 777");

        Branch b4 = new Branch();
        b4.setBranchName("Chi nhánh Bình Thạnh");
        b4.setAddress("89 Điện Biên Phủ");
        b4.setWard("Phường 15");
        b4.setDistrict("Bình Thạnh");
        b4.setCity("TP.HCM");
        b4.setLatitude(10.8008);
        b4.setLongitude(107.7091);
        b4.setPhone("0911 222 333");

        Branch b5 = new Branch();
        b5.setBranchName("Chi nhánh Gò Vấp");
        b5.setAddress("21 Phan Văn Trị");
        b5.setWard("Phường 7");
        b5.setDistrict("Gò Vấp");
        b5.setCity("TP.HCM");
        b5.setLatitude(10.8356);
        b5.setLongitude(108.6648);
        b5.setPhone("0933 123 456");

        Branch b6 = new Branch();
        b6.setBranchName("Chi nhánh Tân Bình");
        b6.setAddress("33 Cộng Hòa");
        b6.setWard("Phường 13");
        b6.setDistrict("Tân Bình");
        b6.setCity("TP.HCM");
        b6.setLatitude(9.8019);
        b6.setLongitude(106.6537);
        b6.setPhone("0966 555 777");

        branches.add(b1);
        branches.add(b2);
        branches.add(b3);
        branches.add(b4);
        branches.add(b5);
        branches.add(b6);

        model.addAttribute("branches", branches);
        return "branch-address";
    }
}
