package org.example.fashion_web.backend.repositories;

import org.example.fashion_web.backend.models.Product;
import org.example.fashion_web.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Optional<User> findById(Long id);

    User findByName(String username);
}
