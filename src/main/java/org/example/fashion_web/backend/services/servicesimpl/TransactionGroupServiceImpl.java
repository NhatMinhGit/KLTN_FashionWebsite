package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.TransactionGroup;
import org.example.fashion_web.backend.repositories.TransactionGroupRepository;
import org.example.fashion_web.backend.services.TransactionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionGroupServiceImpl implements TransactionGroupService {
    @Autowired
    private TransactionGroupRepository transactionGroupRepository;
    @Override
    public TransactionGroup save(TransactionGroup TransactionGroup) {
        return transactionGroupRepository.save(TransactionGroup);
    }

    @Override
    public List<TransactionGroup> getAllTransactionGroups() {
        return transactionGroupRepository.findAll();
    }

    @Override
    public TransactionGroup getTransactionGroupById(Long id) {
        return transactionGroupRepository.findByGroupId(id);
    }


}
