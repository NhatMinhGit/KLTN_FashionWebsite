package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {}