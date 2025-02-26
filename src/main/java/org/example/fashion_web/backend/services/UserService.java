package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;


public interface UserService {
    User save(UserDto userDto);
}
