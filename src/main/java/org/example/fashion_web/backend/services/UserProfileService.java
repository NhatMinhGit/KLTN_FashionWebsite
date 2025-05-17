package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.dto.UserProfileDto;
import org.example.fashion_web.backend.models.UserProfile;

import java.util.Optional;

public interface UserProfileService {
    UserProfile save(UserProfileDto userProfileDto);
    Optional<UserProfile> getUserProfileById(Long id);
    UserProfile findByUserId(Long id);
}
