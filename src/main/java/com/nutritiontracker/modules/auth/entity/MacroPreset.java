package com.nutritiontracker.modules.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "macro_presets", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "name" })
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MacroPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Preset name is required")
    @Size(max = 50, message = "Preset name must not exceed 50 characters")
    private String name;

    @Column(name = "protein_percentage", nullable = false)
    @NotNull(message = "Protein percentage is required")
    @Min(value = 0, message = "Protein percentage must be at least 0")
    @Max(value = 100, message = "Protein percentage must not exceed 100")
    private Integer proteinPercentage;

    @Column(name = "carbs_percentage", nullable = false)
    @NotNull(message = "Carbs percentage is required")
    @Min(value = 0, message = "Carbs percentage must be at least 0")
    @Max(value = 100, message = "Carbs percentage must not exceed 100")
    private Integer carbsPercentage;

    @Column(name = "fats_percentage", nullable = false)
    @NotNull(message = "Fats percentage is required")
    @Min(value = 0, message = "Fats percentage must be at least 0")
    @Max(value = 100, message = "Fats percentage must not exceed 100")
    private Integer fatsPercentage;

    @Column(name = "is_default", nullable = false)
    @lombok.Builder.Default
    private Boolean isDefault = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @AssertTrue(message = "Macro percentages must sum to 100")
    public boolean isMacroSumValid() {
        if (proteinPercentage == null || carbsPercentage == null || fatsPercentage == null) {
            return false;
        }
        return proteinPercentage + carbsPercentage + fatsPercentage == 100;
    }
}
