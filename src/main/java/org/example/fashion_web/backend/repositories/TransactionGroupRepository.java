package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, Long> {
    TransactionGroup findByGroupId(Long id);
}