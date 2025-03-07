package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface UserService {
    User save(UserDto userDto);
    public User addUser(User user);
    Page<User> getAllUsers(Pageable pageable);
    public boolean updateUserStatus(Long id, boolean status);
}
