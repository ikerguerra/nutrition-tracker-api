package com.nutritiontracker.modules.food.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(unique = true, length = 100)
    private String barcode;

    @Column(name = "serving_size", precision = 10, scale = 2)
    private BigDecimal servingSize;

    @Column(name = "serving_unit", length = 50)
    private String servingUnit;

    @Column(length = 50)
    @Builder.Default
    private String source = "LOCAL"; // LOCAL, OPENFOODFACTS

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @OneToOne(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private NutritionalInfo nutritionalInfo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to set bidirectional relationship
    public void setNutritionalInfo(NutritionalInfo nutritionalInfo) {
        if (nutritionalInfo == null) {
            if (this.nutritionalInfo != null) {
                this.nutritionalInfo.setFood(null);
            }
        } else {
            nutritionalInfo.setFood(this);
        }
        this.nutritionalInfo = nutritionalInfo;
    }
}
