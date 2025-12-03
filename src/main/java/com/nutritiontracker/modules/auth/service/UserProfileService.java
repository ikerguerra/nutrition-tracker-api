package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile createDefaultProfile(User user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .preferredUnitSystem("METRIC")
                .preferredLanguage("es")
                .useCustomMacros(false)
                .build();

        return userProfileRepository.save(profile);
    }
}
