package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.TransactionGroup;

import java.util.List;

public interface TransactionGroupService {
    TransactionGroup save(TransactionGroup transactionGroup);

    List<TransactionGroup> getAllTransactionGroups();

    TransactionGroup getTransactionGroupById(Long id);
}
