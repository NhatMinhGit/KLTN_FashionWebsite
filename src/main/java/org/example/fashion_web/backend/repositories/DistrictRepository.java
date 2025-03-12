package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {}