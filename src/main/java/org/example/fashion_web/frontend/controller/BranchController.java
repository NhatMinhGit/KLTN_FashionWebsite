package org.example.fashion_web.frontend.controller;

import org.example.fashion_web.backend.models.Branch;
import org.example.fashion_web.backend.services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BranchController {
    @Autowired
    private BranchService branchService;

    @GetMapping("/branch")
    public String getBranchPage(Model model) {
        List<Branch> branches = branchService.getAllBranches();

        model.addAttribute("branches", branches);
        return "branch-address";
    }
}
