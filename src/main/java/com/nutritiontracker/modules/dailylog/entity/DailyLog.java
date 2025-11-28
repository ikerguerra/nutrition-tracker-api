package com.nutritiontracker.modules.dailylog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "date" })
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private LocalDate date;

    // Aggregated totals
    @Column(name = "total_calories", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCalories = BigDecimal.ZERO;

    @Column(name = "total_protein", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalProtein = BigDecimal.ZERO;

    @Column(name = "total_carbs", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalCarbs = BigDecimal.ZERO;

    @Column(name = "total_fats", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFats = BigDecimal.ZERO;

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealEntry> mealEntries = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper to add entry
    public void addMealEntry(MealEntry entry) {
        mealEntries.add(entry);
        entry.setDailyLog(this);
    }

    // Helper to remove entry
    public void removeMealEntry(MealEntry entry) {
        mealEntries.remove(entry);
        entry.setDailyLog(null);
    }
}
