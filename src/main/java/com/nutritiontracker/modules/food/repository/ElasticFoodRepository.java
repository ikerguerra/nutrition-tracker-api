package com.nutritiontracker.modules.food.repository;

import com.nutritiontracker.modules.food.entity.ElasticFoodDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ElasticFoodRepository {

    public Page<ElasticFoodDocument> fuzzySearch(String query, Pageable pageable) {
        return new PageImpl<>(Collections.emptyList());
    }

    public Page<ElasticFoodDocument> findByBarcode(String barcode, Pageable pageable) {
        return new PageImpl<>(Collections.emptyList());
    }

    public long count() { return 1; } // Fake count to skip sync
    public void saveAll(Iterable<ElasticFoodDocument> docs) {}
    public void save(ElasticFoodDocument doc) {}
    public void deleteById(String id) {}
}
