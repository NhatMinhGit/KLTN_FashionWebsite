package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.UserDto;
import org.example.fashion_web.backend.models.User;
import org.example.fashion_web.backend.repositories.UserRepository;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    @Override
    public User findByUsername(String username) {
        return userRepository.findByName(username);
    }

    @Override
    public List<User> getAllUsersExceptCurrent() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.getEmail().equals(currentUsername))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findUsersByRole(role);
    }

    @Override
    public int getUserCount(String role) {
        return userRepository.findUsersByRole(role).size();
    }

    @Override
    public List<User> searchUsersByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return userRepository.findAll();

        String lowerKeyword = keyword.toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> String.valueOf(u.getId()).equals(keyword) ||
                        u.getName().toLowerCase().contains(lowerKeyword) ||
                        u.getEmail().toLowerCase().contains(lowerKeyword) ||
                        u.getRole().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }


}

