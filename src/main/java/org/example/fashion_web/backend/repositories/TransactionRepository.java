package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Transaction;
import org.example.fashion_web.backend.models.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findAllByTransactionGroup(TransactionGroup transactionGroup);

}
