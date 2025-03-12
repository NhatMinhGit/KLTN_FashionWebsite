package org.example.fashion_web.backend.services;

import org.example.fashion_web.backend.models.UserProfile;

import java.util.Optional;

public interface UserProfileService {

    Optional<UserProfile> getUserProfileById(Long id);
}
