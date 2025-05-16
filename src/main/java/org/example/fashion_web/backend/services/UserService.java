package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface UserService {
    User save(UserDto userDto);
    User findByEmail(String email);
    User addUser(User user);
    Page<User> getAllUsers(Pageable pageable);
    boolean updateUserStatus(Long id, boolean status);

    List<User> findAll();
    Optional<User> findById(Long id);
    User findByUsername(String username);

    List<User> getAllUsersExceptCurrent();

    List<User> getUsersByRole(String role);

    int getUserCount(String role);

    List<User> searchUsersByKeyword(String keyword);
}
