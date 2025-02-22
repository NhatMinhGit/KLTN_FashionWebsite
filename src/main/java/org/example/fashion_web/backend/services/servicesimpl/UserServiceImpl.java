package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public User save(UserDto userDto) {
        User user = new User(userDto.getAddress(), userDto.getPhoneNumber(), userDto.getRole(), passwordEncoder.encode(userDto.getPassword()),userDto.getEmail(), userDto.getName() );
        return userRepository.save(user);
    }
}
