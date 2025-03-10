package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Override
    public User save(UserDto userDto) {
        User user = new User(userDto.getRole(), passwordEncoder.encode(userDto.getPassword()),userDto.getEmail(), userDto.getName() );
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User addUser(User user) {
        if (user != null) {
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public boolean updateUserStatus(Long id, boolean status) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setStatus(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

}

