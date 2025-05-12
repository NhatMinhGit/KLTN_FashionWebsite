package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.Transaction;
import org.example.fashion_web.backend.models.TransactionGroup;

import java.util.List;

public interface TransactionService {
    Transaction save(Transaction transaction);

    List<Transaction> getTransactionsByGroupId(TransactionGroup transactionGroup);
}
