package com.nutritiontracker.modules.food.repository;

import com.nutritiontracker.modules.food.entity.ElasticFoodDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticFoodRepository extends ElasticsearchRepository<ElasticFoodDocument, String> {

    @org.springframework.data.elasticsearch.annotations.Query("{\"bool\": {\"should\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"brand\"], \"fuzziness\": \"AUTO\"}}, {\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"brand\"], \"type\": \"bool_prefix\"}}]}}")
    Page<ElasticFoodDocument> fuzzySearch(@org.springframework.data.repository.query.Param("query") String query,
            Pageable pageable);

    Page<ElasticFoodDocument> findByBarcode(String barcode, Pageable pageable);
}
