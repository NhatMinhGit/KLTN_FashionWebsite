package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
}
