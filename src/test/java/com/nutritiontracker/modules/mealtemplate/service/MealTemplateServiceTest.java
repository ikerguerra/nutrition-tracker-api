package com.nutritiontracker.modules.mealtemplate.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.service.DailyLogService;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplateItem;
import com.nutritiontracker.modules.mealtemplate.repository.MealTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealTemplate Service Unit Tests")
class MealTemplateServiceTest {

    @Mock
    private MealTemplateRepository mealTemplateRepository;

    @Mock
    private DailyLogService dailyLogService;

    @InjectMocks
    private MealTemplateService mealTemplateService;

    @Test
    @DisplayName("getUserTemplates should return templates from repository")
    void shouldGetUserTemplates() {
        when(mealTemplateRepository.findByUserIdOrSystemTrue(1L)).thenReturn(List.of(new MealTemplate()));
        List<MealTemplate> result = mealTemplateService.getUserTemplates(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getTemplateById should return template when exists")
    void shouldGetTemplateById() {
        MealTemplate template = new MealTemplate();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(template));
        MealTemplate result = mealTemplateService.getTemplateById(1L);
        assertThat(result).isEqualTo(template);
    }

    @Test
    @DisplayName("getTemplateById should throw exception when not found")
    void shouldThrowWhenTemplateNotFound() {
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mealTemplateService.getTemplateById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createTemplate should save template to repository")
    void shouldCreateTemplate() {
        MealTemplate template = MealTemplate.builder().name("New Template").build();
        when(mealTemplateRepository.save(template)).thenReturn(template);

        MealTemplate result = mealTemplateService.createTemplate(template);

        assertThat(result).isEqualTo(template);
        verify(mealTemplateRepository).save(template);
    }

    @Test
    @DisplayName("updateTemplate should throw if system template")
    void shouldThrowWhenUpdatingSystemTemplate() {
        MealTemplate existing = MealTemplate.builder().id(1L).isSystem(true).build();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> mealTemplateService.updateTemplate(1L, new MealTemplate(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update a system template");
    }

    @Test
    @DisplayName("updateTemplate should update fields and replace items")
    void shouldUpdateTemplate() {
        MealTemplate existing = MealTemplate.builder()
                .id(1L)
                .userId(1L)
                .isSystem(false)
                .items(new ArrayList<>())
                .build();

        MealTemplate updated = MealTemplate.builder()
                .name("New Name")
                .items(List.of(MealTemplateItem.builder().unit("g").quantity(BigDecimal.ONE).build()))
                .build();

        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));
        when(mealTemplateRepository.save(any(MealTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MealTemplate result = mealTemplateService.updateTemplate(1L, updated, 1L);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getItems()).hasSize(1);
        verify(mealTemplateRepository).save(existing);
    }

    @Test
    @DisplayName("updateTemplate should throw if not owner")
    void shouldThrowWhenUpdatingNotOwnedTemplate() {
        MealTemplate existing = MealTemplate.builder().id(1L).userId(2L).isSystem(false).build();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> mealTemplateService.updateTemplate(1L, new MealTemplate(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User does not own this template");
    }

    @Test
    @DisplayName("deleteTemplate should remove template from repository")
    void shouldDeleteTemplate() {
        MealTemplate existing = MealTemplate.builder().id(1L).userId(1L).isSystem(false).build();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));

        mealTemplateService.deleteTemplate(1L, 1L);

        verify(mealTemplateRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTemplate should throw if system template")
    void shouldThrowWhenDeletingSystemTemplate() {
        MealTemplate existing = MealTemplate.builder().id(1L).isSystem(true).build();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> mealTemplateService.deleteTemplate(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete a system template");
    }

    @Test
    @DisplayName("deleteTemplate should throw if not owner")
    void shouldThrowWhenDeletingNotOwnedTemplate() {
        MealTemplate existing = MealTemplate.builder().id(1L).userId(2L).isSystem(false).build();
        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> mealTemplateService.deleteTemplate(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User does not own this template");
    }

    @Test
    @DisplayName("applyTemplate should call dailyLogService for each item")
    void shouldApplyTemplate() {
        Food food = new Food();
        food.setId(10L);

        MealTemplateItem item = MealTemplateItem.builder()
                .food(food)
                .quantity(BigDecimal.valueOf(100))
                .unit("g")
                .build();

        MealTemplate template = MealTemplate.builder()
                .id(1L)
                .mealType(MealType.LUNCH)
                .items(List.of(item))
                .build();

        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(template));

        LocalDate date = LocalDate.now();
        mealTemplateService.applyTemplate(1L, date, null, 1L);

        verify(dailyLogService).addEntry(argThat(request -> request.getFoodId().equals(10L) &&
                request.getDate().equals(date) &&
                request.getMealType() == MealType.LUNCH), eq(1L));
    }

    @Test
    @DisplayName("applyTemplate should use default meal type if not specified")
    void shouldApplyTemplateWithDefaultMealType() {
        Food food = new Food();
        food.setId(10L);

        MealTemplateItem item = MealTemplateItem.builder()
                .food(food)
                .quantity(BigDecimal.valueOf(100))
                .unit("g")
                .build();

        // Template with NO meal type
        MealTemplate template = MealTemplate.builder()
                .id(1L)
                .mealType(null)
                .items(List.of(item))
                .build();

        when(mealTemplateRepository.findByIdWithItems(1L)).thenReturn(Optional.of(template));

        LocalDate date = LocalDate.now();
        mealTemplateService.applyTemplate(1L, date, null, 1L);

        verify(dailyLogService).addEntry(argThat(request -> request.getMealType() == MealType.BREAKFAST), eq(1L));
    }
}
