package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.models.Transaction;
import org.example.fashion_web.backend.models.TransactionGroup;
import org.example.fashion_web.backend.repositories.TransactionRepository;
import org.example.fashion_web.backend.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    // Phương thức lưu giao dịch
    @Override
    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);  // Dùng repository để lưu vào cơ sở dữ liệu
    }

    @Override
    public List<Transaction> getTransactionsByGroupId(TransactionGroup transactionGroup) {
        return transactionRepository.findAllByTransactionGroup(transactionGroup);
    }
}
