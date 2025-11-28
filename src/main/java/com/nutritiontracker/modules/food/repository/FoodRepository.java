package com.nutritiontracker.modules.food.repository;

import com.nutritiontracker.modules.food.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * Find food by barcode
     */
    Optional<Food> findByBarcode(String barcode);

    /**
     * Check if barcode exists
     */
    boolean existsByBarcode(String barcode);

    /**
     * Search foods by name (case-insensitive, partial match)
     */
    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Food> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * Search foods by brand (case-insensitive, partial match)
     */
    @Query("SELECT f FROM Food f WHERE LOWER(f.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    Page<Food> searchByBrand(@Param("brand") String brand, Pageable pageable);

    /**
     * Search foods by name or brand
     */
    @Query("SELECT f FROM Food f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(f.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Food> searchByNameOrBrand(@Param("query") String query, Pageable pageable);

    /**
     * Find food with nutritional info eagerly loaded
     */
    @Query("SELECT f FROM Food f LEFT JOIN FETCH f.nutritionalInfo WHERE f.id = :id")
    Optional<Food> findByIdWithNutritionalInfo(@Param("id") Long id);

    /**
     * Find food by barcode with nutritional info eagerly loaded
     */
    @Query("SELECT f FROM Food f LEFT JOIN FETCH f.nutritionalInfo WHERE f.barcode = :barcode")
    Optional<Food> findByBarcodeWithNutritionalInfo(@Param("barcode") String barcode);
}
