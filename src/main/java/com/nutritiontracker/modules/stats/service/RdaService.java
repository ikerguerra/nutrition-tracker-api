package com.nutritiontracker.modules.stats.service;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.stats.dto.RdaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RdaService {

    private final UserProfileRepository userProfileRepository;

    public RdaDto getRdaForUser(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", userId));

        String gender = profile.getGender() != null ? profile.getGender().toUpperCase() : "MALE"; // Default
        Integer age = profile.getAge(); // Can be null
        if (age == null)
            age = 30; // Default

        boolean isMale = "MALE".equals(gender);

        return RdaDto.builder()
                .fiber(BigDecimal.valueOf(isMale ? 38 : 25))
                .sugars(BigDecimal.valueOf(isMale ? 36 : 25)) // Added sugar limits approx
                .saturatedFats(BigDecimal.valueOf(20)) // General guideline
                .sodium(BigDecimal.valueOf(2300))
                .calcium(BigDecimal.valueOf(age > 50 ? 1200 : 1000))
                .iron(BigDecimal.valueOf(calculateIron(isMale, age)))
                .potassium(BigDecimal.valueOf(isMale ? 3400 : 2600))
                .vitaminA(BigDecimal.valueOf(isMale ? 3000 : 2333)) // Approx IU
                .vitaminC(BigDecimal.valueOf(isMale ? 90 : 75))
                .vitaminD(BigDecimal.valueOf(600))
                .vitaminE(BigDecimal.valueOf(15))
                .vitaminB12(BigDecimal.valueOf(2.4))
                .magnesium(BigDecimal.valueOf(isMale ? 400 : 310))
                .zinc(BigDecimal.valueOf(isMale ? 11 : 8))
                .vitaminK(BigDecimal.valueOf(isMale ? 120 : 90))
                .vitaminB1(BigDecimal.valueOf(isMale ? 1.2 : 1.1)) // Thiamine
                .vitaminB2(BigDecimal.valueOf(isMale ? 1.3 : 1.1)) // Riboflavin
                .vitaminB3(BigDecimal.valueOf(isMale ? 16 : 14)) // Niacin
                .vitaminB6(BigDecimal.valueOf(isMale ? 1.3 : 1.3)) // (1.7/1.5 if >50, keep simple for now)
                .vitaminB9(BigDecimal.valueOf(400)) // Folate
                .build();
    }

    private double calculateIron(boolean isMale, int age) {
        if (isMale)
            return 8.0;
        if (age > 50)
            return 8.0;
        return 18.0; // Female 19-50
    }
}
