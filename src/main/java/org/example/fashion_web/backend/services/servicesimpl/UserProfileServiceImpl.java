package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.dto.UserProfileDto;
import org.example.fashion_web.backend.models.UserProfile;
import org.example.fashion_web.backend.repositories.UserProfileRepository;
import org.example.fashion_web.backend.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    UserProfileRepository userProfileRepository;


    @Override
    public UserProfile save(UserProfileDto userProfileDto) {
        UserProfile userProfile = new UserProfile(userProfileDto.getUser(), userProfileDto.getWard(), userProfileDto.getDob(), userProfileDto.getAvatar(), userProfileDto.getPhoneNumber(), userProfileDto.getAddress());
        return userProfileRepository.save(userProfile);
    }

    @Override
    public Optional<UserProfile> getUserProfileById(Long id) {
        return userProfileRepository.findById(id);
    }

    @Override
    public UserProfile findByUserId(Long id) {
        return userProfileRepository.findByUser_Id(id);
    }
}
