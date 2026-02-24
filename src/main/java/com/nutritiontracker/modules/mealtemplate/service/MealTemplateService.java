package com.nutritiontracker.modules.mealtemplate.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.service.DailyLogService;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplateItem;
import com.nutritiontracker.modules.mealtemplate.repository.MealTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealTemplateService {

    private final MealTemplateRepository mealTemplateRepository;
    private final DailyLogService dailyLogService;

    @Transactional(readOnly = true)
    @Cacheable(value = "mealTemplates", key = "#userId")
    public List<MealTemplate> getUserTemplates(Long userId) {
        return mealTemplateRepository.findByUserIdOrSystemTrue(userId);
    }

    @Transactional(readOnly = true)
    public MealTemplate getTemplateById(Long id) {
        return mealTemplateRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("MealTemplate", id));
    }

    @Transactional
    @CacheEvict(value = "mealTemplates", allEntries = true)
    public MealTemplate createTemplate(MealTemplate template) {
        log.info("Creating new meal template: {}", template.getName());
        return mealTemplateRepository.save(template);
    }

    @Transactional
    @CacheEvict(value = "mealTemplates", allEntries = true)
    public MealTemplate updateTemplate(Long id, MealTemplate updatedTemplate, Long userId) {
        MealTemplate existing = getTemplateById(id);

        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new IllegalArgumentException("Cannot update a system template");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this template");
        }

        existing.setName(updatedTemplate.getName());
        existing.setDescription(updatedTemplate.getDescription());
        existing.setMealType(updatedTemplate.getMealType());
        existing.setIsPublic(updatedTemplate.getIsPublic());

        // Update items - simple replacement for now
        existing.getItems().clear();
        for (MealTemplateItem item : updatedTemplate.getItems()) {
            existing.addItem(item);
        }

        return mealTemplateRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "mealTemplates", allEntries = true)
    public void deleteTemplate(Long id, Long userId) {
        MealTemplate existing = getTemplateById(id);

        if (Boolean.TRUE.equals(existing.getIsSystem())) {
            throw new IllegalArgumentException("Cannot delete a system template");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this template");
        }

        mealTemplateRepository.deleteById(id);
    }

    @Transactional
    public void applyTemplate(Long templateId, LocalDate date, MealType mealType, Long userId) {
        log.info("Applying template {} to date {} for meal type {}", templateId, date, mealType);
        MealTemplate template = getTemplateById(templateId);

        MealType targetMealType = mealType != null ? mealType : template.getMealType();
        if (targetMealType == null) {
            targetMealType = MealType.BREAKFAST; // Default if nothing specified
        }

        for (MealTemplateItem item : template.getItems()) {
            MealEntryRequestDto request = MealEntryRequestDto.builder()
                    .date(date)
                    .foodId(item.getFood().getId())
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .mealType(targetMealType)
                    .build();

            dailyLogService.addEntry(request, userId);
        }
    }
}
