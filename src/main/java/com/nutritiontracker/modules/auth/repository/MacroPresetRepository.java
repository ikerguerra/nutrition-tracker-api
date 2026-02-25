package com.nutritiontracker.modules.auth.repository;

import com.nutritiontracker.modules.auth.entity.MacroPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MacroPresetRepository extends JpaRepository<MacroPreset, Long> {

    List<MacroPreset> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<MacroPreset> findByUserIdAndIsDefaultTrue(Long userId);

    boolean existsByUserIdAndName(Long userId, String name);

    Optional<MacroPreset> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
