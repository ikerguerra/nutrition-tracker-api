package com.nutritiontracker.modules.food.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BarcodeServiceReplicationTest {

    @Autowired
    private BarcodeService barcodeService;

    @Test
    public void testReplicateBarcodeError() {
        barcodeService.searchByBarcode("8480000808592");
    }
}
