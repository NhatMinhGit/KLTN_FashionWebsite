package org.example.fashion_web.backend.services;


import org.example.fashion_web.backend.models.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(int userId);
    List<User> getAllUsers();
    User updateUser(int userId, User user);
    void deleteUser(int userId);
}