package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Branch;
import org.example.fashion_web.backend.repositories.BranchRepository;
import org.example.fashion_web.backend.services.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchServiceImpl implements BranchService {
    @Autowired
    private BranchRepository branchRepository;
    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}
