package com.nutritiontracker.modules.mealtemplate.repository;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplateItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("MealTemplate Repository Integration Tests")
class MealTemplateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MealTemplateRepository mealTemplateRepository;

    private Food persistedFood;

    @BeforeEach
    void setUp() {
        Food food = new Food();
        food.setName("Test Food");
        food.setServingSize(BigDecimal.valueOf(100));
        food.setServingUnit("g");
        persistedFood = entityManager.persistAndFlush(food);
    }

    private MealTemplate buildTemplate(Long userId, boolean isSystem, boolean isPublic) {
        return MealTemplate.builder()
                .userId(userId)
                .name("Template " + (isSystem ? "System" : "User"))
                .isSystem(isSystem)
                .isPublic(isPublic)
                .build();
    }

    @Test
    @DisplayName("findByUserIdOrSystemTrue should return user templates and system templates")
    void shouldFindByUserIdOrSystemTrue() {
        MealTemplate userTemplate = buildTemplate(1L, false, false);
        MealTemplate systemTemplate = buildTemplate(null, true, false);
        MealTemplate otherUserTemplate = buildTemplate(2L, false, false);

        entityManager.persist(userTemplate);
        entityManager.persist(systemTemplate);
        entityManager.persist(otherUserTemplate);
        entityManager.flush();
        entityManager.clear();

        List<MealTemplate> results = mealTemplateRepository.findByUserIdOrSystemTrue(1L);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(MealTemplate::getName)
                .containsExactlyInAnyOrder("Template User", "Template System");
    }

    @Test
    @DisplayName("findByIdWithItems should return template with items and food loaded")
    void shouldFindByIdWithItems() {
        MealTemplate template = buildTemplate(1L, false, false);
        MealTemplateItem item = MealTemplateItem.builder()
                .food(persistedFood)
                .quantity(BigDecimal.valueOf(200))
                .unit("g")
                .build();
        template.addItem(item);

        MealTemplate saved = entityManager.persistAndFlush(template);
        entityManager.clear();

        Optional<MealTemplate> result = mealTemplateRepository.findByIdWithItems(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getItems()).hasSize(1);
        assertThat(result.get().getItems().get(0).getFood().getName()).isEqualTo("Test Food");
    }

    @Test
    @DisplayName("findPublicOrUserOrSystemTemplates should return correct visibility")
    void shouldFindPublicOrUserOrSystemTemplates() {
        MealTemplate publicTemplate = buildTemplate(99L, false, true);
        MealTemplate privateTemplate = buildTemplate(99L, false, false);
        MealTemplate myTemplate = buildTemplate(1L, false, false);
        MealTemplate systemTemplate = buildTemplate(null, true, false);

        entityManager.persist(publicTemplate);
        entityManager.persist(privateTemplate);
        entityManager.persist(myTemplate);
        entityManager.persist(systemTemplate);
        entityManager.flush();
        entityManager.clear();

        List<MealTemplate> results = mealTemplateRepository.findPublicOrUserOrSystemTemplates(1L);

        assertThat(results).hasSize(3);
        assertThat(results).extracting(MealTemplate::getName)
                .containsExactlyInAnyOrder("Template User", "Template System", "Template User"); // names are generic in
                                                                                                 // builder
    }

    @Test
    @DisplayName("deleteByUserId should remove user templates")
    void shouldDeleteByUserId() {
        MealTemplate t1 = buildTemplate(5L, false, false);
        MealTemplate t2 = buildTemplate(5L, false, false);
        entityManager.persistAndFlush(t1);
        entityManager.persistAndFlush(t2);

        mealTemplateRepository.deleteByUserId(5L);
        entityManager.flush();

        List<MealTemplate> remaining = mealTemplateRepository.findByUserIdOrSystemTrue(5L);
        // Only system templates should remain (none created for user 5 in this test)
        assertThat(remaining).isEmpty();
    }
}
