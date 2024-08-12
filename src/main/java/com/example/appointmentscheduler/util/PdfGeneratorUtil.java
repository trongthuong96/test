package com.example.appointmentscheduler.util;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.Invoice;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import com.itextpdf.text.DocumentException;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class PdfGeneratorUtil {

    private final SpringTemplateEngine templateEngine;
    private final String baseUrl;

    public PdfGeneratorUtil(SpringTemplateEngine templateEngine, @Value("${base.url}") String baseUrl) {
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
    }

    public File generatePdfFromInvoice(Invoice invoice) {
        // Đường dẫn tới font Times New Roman
        String fontPath = "static/font/Times New Roman 400.ttf";
        Context ctx = new Context();
        ctx.setVariable("invoice", invoice);
        String processedHtml = templateEngine.process("email/pdf/invoice", ctx);

        ITextRenderer renderer = new ITextRenderer();
        try {
            // Tạo một BaseFont từ file font
            BaseFont baseFont  = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            // Tạo một đối tượng Font từ BaseFont
            com.itextpdf.text.Font font = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.BOLD, BaseColor.BLACK);
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.setDocumentFromString(processedHtml, baseUrl);
            renderer.layout();

            String fileName = UUID.randomUUID().toString();
            FileOutputStream os = null;
            try {
                final File outputFile = File.createTempFile(fileName, ".pdf");
                os = new FileOutputStream(outputFile);
                renderer.createPDF(os, false);
                renderer.finishPDF();
                return outputFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        return null;
    }

    public File generatePdfFromAppointment(Appointment appointment) {
        // Đường dẫn tới font Times New Roman
        String fontPath = "static/font/Times New Roman 400.ttf";

        Context ctx = new Context();
        ctx.setVariable("appointment", appointment);
        String processedHtml = templateEngine.process("email/pdf/appointment", ctx);

        ITextRenderer renderer = new ITextRenderer();
        try{
            // Tạo một BaseFont từ file font
            BaseFont baseFont  = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            // Tạo một đối tượng Font từ BaseFont
            // Tạo một đối tượng Font từ BaseFont
            com.itextpdf.text.Font font = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.BOLD, BaseColor.BLACK);
            renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            renderer.setDocumentFromString(processedHtml, baseUrl);
            renderer.layout();

            String fileName ="Lich_hen" + UUID.randomUUID().toString();
            FileOutputStream os = null;
            try {
                final File outputFile = File.createTempFile(fileName, ".pdf");
                os = new FileOutputStream(outputFile);
                renderer.createPDF(os, false);
                renderer.finishPDF();
                return outputFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return null;
    }

}
