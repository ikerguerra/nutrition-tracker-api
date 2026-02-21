package com.nutritiontracker.modules.report.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import com.nutritiontracker.modules.dailylog.entity.MealEntry;
import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final DailyLogRepository dailyLogRepository;

    @Transactional(readOnly = true)
    public byte[] generateCsvReport(Long userId, LocalDate startDate, LocalDate endDate) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetweenWithEntries(userId, startDate, endDate);

        try (StringWriter sw = new StringWriter();
                CSVPrinter csvPrinter = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                        .setHeader("Date", "Meal Type", "Food", "Serving Size", "Serving Unit", "Calories",
                                "Protein (g)", "Carbs (g)", "Fat (g)")
                        .build())) {

            for (DailyLog log : logs) {
                if (log.getMealEntries() == null || log.getMealEntries().isEmpty()) {
                    csvPrinter.printRecord(log.getDate(), "N/A", "No entries", "", "",
                            log.getTotalCalories(), log.getTotalProtein(), log.getTotalCarbs(), log.getTotalFats());
                    continue;
                }

                for (MealEntry entry : log.getMealEntries()) {
                    csvPrinter.printRecord(
                            log.getDate(),
                            entry.getMealType(),
                            entry.getFood() != null ? entry.getFood().getName() : "Custom/Unknown",
                            entry.getQuantity(),
                            entry.getUnit(),
                            entry.getCalories(),
                            entry.getProtein(),
                            entry.getCarbohydrates(),
                            entry.getFats());
                }
            }

            csvPrinter.flush();
            return sw.toString().getBytes();
        } catch (IOException e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Error generating CSV report", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generatePdfReport(Long userId, LocalDate startDate, LocalDate endDate) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetweenWithEntries(userId, startDate, endDate);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Nutrition Tracker Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph subtitle = new Paragraph("From: " + startDate + " To: " + endDate, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            for (DailyLog logItem : logs) {
                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph dateTitle = new Paragraph("Date: " + logItem.getDate().toString(), dateFont);
                dateTitle.setSpacingBefore(10);
                dateTitle.setSpacingAfter(5);
                document.add(dateTitle);

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 2f, 3f, 1.5f, 1.5f, 1.5f, 1.5f });

                addTableHeader(table, "Meal Type", "Food", "Calories", "Protein", "Carbs", "Fat");

                if (logItem.getMealEntries() == null || logItem.getMealEntries().isEmpty()) {
                    PdfPCell cell = new PdfPCell(new Phrase("No entries for this date"));
                    cell.setColspan(6);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                } else {
                    for (MealEntry entry : logItem.getMealEntries()) {
                        table.addCell(entry.getMealType().toString());
                        table.addCell(entry.getFood() != null ? entry.getFood().getName() : "Custom");
                        table.addCell(String.valueOf(entry.getCalories()));
                        table.addCell(String.valueOf(entry.getProtein()));
                        table.addCell(String.valueOf(entry.getCarbohydrates()));
                        table.addCell(String.valueOf(entry.getFats()));
                    }
                }

                // Add daily totals row
                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
                PdfPCell totalCell = new PdfPCell(new Phrase("Daily Totals", totalFont));
                totalCell.setColspan(2);
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(totalCell);

                table.addCell(new Phrase(String.valueOf(logItem.getTotalCalories()), totalFont));
                table.addCell(new Phrase(String.valueOf(logItem.getTotalProtein()), totalFont));
                table.addCell(new Phrase(String.valueOf(logItem.getTotalCarbs()), totalFont));
                table.addCell(new Phrase(String.valueOf(logItem.getTotalFats()), totalFont));

                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        for (String headerTitle : headers) {
            PdfPCell header = new PdfPCell();
            header.setPhrase(new Phrase(headerTitle, headerFont));
            table.addCell(header);
        }
    }
}
