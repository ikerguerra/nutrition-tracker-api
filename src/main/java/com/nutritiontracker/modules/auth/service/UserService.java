package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.enums.AuthProvider;
import com.nutritiontracker.modules.auth.repository.UserRepository;
import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import com.nutritiontracker.modules.food.repository.FavoriteFoodRepository;
import com.nutritiontracker.modules.mealtemplate.repository.MealTemplateRepository;
import com.nutritiontracker.modules.recipe.repository.RecipeRepository;
import com.nutritiontracker.modules.recommendation.repository.DietPlanRepository;
import com.nutritiontracker.modules.recommendation.repository.DietRecommendationRepository;
import com.nutritiontracker.modules.achievement.repository.AchievementRepository;
import com.nutritiontracker.modules.auth.repository.MacroPresetRepository;
import com.nutritiontracker.modules.notification.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailyLogRepository dailyLogRepository;
    private final MealTemplateRepository mealTemplateRepository;
    private final RecipeRepository recipeRepository;
    private final FavoriteFoodRepository favoriteFoodRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DietRecommendationRepository dietRecommendationRepository;
    private final AchievementRepository achievementRepository;
    private final MacroPresetRepository macroPresetRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate password if user registered locally
        if (user.getProvider() == AuthProvider.LOCAL) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        // Delete dependencies lacking ON DELETE CASCADE
        dailyLogRepository.deleteByUserId(userId);
        mealTemplateRepository.deleteByUserId(userId);
        recipeRepository.deleteByUserId(userId);
        favoriteFoodRepository.deleteByUserId(userId);
        dietPlanRepository.deleteByUserId(userId);
        dietRecommendationRepository.deleteByUserId(userId);
        achievementRepository.deleteByUserId(userId);
        macroPresetRepository.deleteByUserId(userId);
        pushSubscriptionRepository.deleteByUserId(userId);

        // Delete user (will cascade to UserProfile)
        userRepository.delete(user);
    }
}
