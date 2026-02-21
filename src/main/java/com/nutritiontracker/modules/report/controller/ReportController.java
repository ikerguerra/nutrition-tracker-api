package com.nutritiontracker.modules.report.controller;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<byte[]> getCsvReport(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] csvBytes = reportService.generateCsvReport(user.getId(), startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=nutrition_report_" + startDate + "_to_" + endDate + ".csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdfReport(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfBytes = reportService.generatePdfReport(user.getId(), startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=nutrition_report_" + startDate + "_to_" + endDate + ".pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
