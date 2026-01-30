package gt.com.xfactory.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import gt.com.xfactory.dto.response.*;
import jakarta.enterprise.context.*;
import lombok.extern.slf4j.*;

import java.awt.Color;
import java.io.*;
import java.time.format.*;

@ApplicationScoped
@Slf4j
public class PdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    public byte[] generatePrescriptionPdf(PrescriptionDto prescription) {
        log.info("Generating PDF for prescription: {}", prescription.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("RECETA MÉDICA", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Doctor info
            document.add(new Paragraph("Doctor: " + prescription.getDoctorName(), HEADER_FONT));
            document.add(new Paragraph(" "));

            // Patient info
            document.add(new Paragraph("Paciente: " + prescription.getPatientName(), HEADER_FONT));
            document.add(new Paragraph(" "));

            // Dates
            String issueDate = prescription.getIssueDate() != null
                    ? prescription.getIssueDate().format(DATE_FORMAT) : "N/A";
            String expiryDate = prescription.getExpiryDate() != null
                    ? prescription.getExpiryDate().format(DATE_FORMAT) : "N/A";
            document.add(new Paragraph("Fecha de emisión: " + issueDate, NORMAL_FONT));
            document.add(new Paragraph("Fecha de vencimiento: " + expiryDate, NORMAL_FONT));
            document.add(new Paragraph(" "));

            // Medications table
            if (prescription.getMedications() != null && !prescription.getMedications().isEmpty()) {
                document.add(new Paragraph("Medicamentos:", HEADER_FONT));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{3f, 2f, 2f, 2f, 2f, 2f, 1.5f});

                addTableHeader(table, "Medicamento");
                addTableHeader(table, "Concentración");
                addTableHeader(table, "Presentación");
                addTableHeader(table, "Dosis");
                addTableHeader(table, "Frecuencia");
                addTableHeader(table, "Duración");
                addTableHeader(table, "Cantidad");

                for (PrescriptionMedicationDto med : prescription.getMedications()) {
                    addTableCell(table, med.getMedicationName());
                    addTableCell(table, med.getConcentration());
                    addTableCell(table, med.getPresentation() != null ? med.getPresentation().name() : "");
                    addTableCell(table, med.getDose());
                    addTableCell(table, med.getFrequency());
                    addTableCell(table, med.getDuration());
                    addTableCell(table, med.getQuantity() != null ? med.getQuantity().toString() : "");
                }

                document.add(table);
                document.add(new Paragraph(" "));
            }

            // Notes
            if (prescription.getNotes() != null && !prescription.getNotes().isBlank()) {
                document.add(new Paragraph("Notas:", HEADER_FONT));
                document.add(new Paragraph(prescription.getNotes(), NORMAL_FONT));
                document.add(new Paragraph(" "));
            }

            // Signature line
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph signatureLine = new Paragraph("_________________________________", NORMAL_FONT);
            signatureLine.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureLine);

            Paragraph signatureName = new Paragraph("Dr. " + prescription.getDoctorName(), SMALL_FONT);
            signatureName.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureName);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating prescription PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating prescription PDF", e);
        }
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(new Color(60, 60, 60));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", SMALL_FONT));
        cell.setPadding(4f);
        table.addCell(cell);
    }
}
